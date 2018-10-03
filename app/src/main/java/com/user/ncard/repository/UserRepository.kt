package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.user.ncard.AppExecutors
import com.user.ncard.R
import com.user.ncard.SingleLiveEvent
import com.user.ncard.api.*
import com.user.ncard.db.FriendDao
import com.user.ncard.db.NCardInfoDao
import com.user.ncard.db.NcardDb
import com.user.ncard.db.UserDao
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.ui.card.CardsFragment
import com.user.ncard.ui.chats.PrivacyListEvent
import com.user.ncard.util.*
import com.user.ncard.vo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class UserRepository @Inject constructor(val userDao: UserDao, val nCardInfoDao: NCardInfoDao, val context: Context, val db: NcardDb, val friendDao: FriendDao,
                                         val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                         val nCardService: NCardService, val appExecutors: AppExecutors,
                                         val sharedPreferenceHelper: SharedPreferenceHelper, val transferUtility: TransferUtility) {
    //username as email
    val userIds = ArrayList<Int>()
    val userByJobIds = ArrayList<Int>()
    val userIdsFromContact = ArrayList<Int>()

    fun getUserByUserName(userName: String): LiveData<Resource<User>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<User, User>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: User) {
                userDao.insert(item)
            }

            override fun shouldFetch(data: User?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<User> {

                return userDao.findByUsername(userName)
            }

            override fun createCall(): LiveData<ApiResponse<User>> {
                return nCardService.getUser(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USERNAME))
            }
        }.asLiveData()
    }

    fun getUserFromQRCode(userName: String, userLiveData: MutableLiveData<User>, getUserSuccessEvent: SingleLiveEvent<Void>, getUserFailureEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.getUserSingle(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), userName).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ userData ->
                            getUserSuccessEvent.call()
                            userData.user.apply {
                                userLiveData.value = User(id, email, username, phoneNumber, firstName, lastName,
                                        profileImageUrl, thumbnailUrl, summary, country, city, address, postcode, gender, nationality,
                                        userData.status, birthday, remark, createdAt, updatedAt, qualification, chatId, chatPassword, mutualFriendCount, null)
                            }
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    Log.e(TAG, "Unable to get user", error)
                                    getUserFailureEvent.call()
                                    compositeDisposable.dispose()
                                })
                )
            }
        })
    }

    fun getUserFromContact(): LiveData<Resource<List<User>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        val contactList = ArrayList<Contact>()
        val contactWithNameList = ArrayList<ContactWithName>()
        getContactList(contactList, contactWithNameList)
        return object : NetworkBoundResource<List<User>, List<SearchUserApiResponse>>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: List<SearchUserApiResponse>) {
                val userList = ArrayList<User>()
                userIdsFromContact.clear()
                item.forEach {
                    it.user.status = it.status
                    userList.add(it.user)
                    userIdsFromContact.add(it.user.id)
                    val contactWithSameEmail = contactWithNameList.filter { contactWithName ->
                        !it.user.email.isNullOrBlank() && contactWithName.email.contains(it.user.email)
                    }
                    if (contactWithSameEmail.isNotEmpty()) it.user.nameInContact = contactWithSameEmail[0].name
                    else {
                        val contactWithSameMobileNumber = contactWithNameList.filter { contactWithName ->
                            !it.user.phoneNumber.isNullOrBlank() && contactWithName.mobile.contains(it.user.phoneNumber)
                        }
                        if (contactWithSameMobileNumber.isNotEmpty()) it.user.nameInContact = contactWithSameMobileNumber[0].name
                    }
                }
                userDao.insertUsers(userList)
            }

            override fun shouldFetch(data: List<User>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<User>> {
                return if (userIdsFromContact.size == 0) AbsentLiveData.create()
                else userDao.findByUserIds(userIdsFromContact)
            }

            override fun createCall(): LiveData<ApiResponse<List<SearchUserApiResponse>>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.getUsersFromContact(userId, contactList)
            }
        }.asLiveData()
    }

    private fun getContactList(contactList: ArrayList<Contact>, contactWithNameList: ArrayList<ContactWithName>) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {

            do {
                // get the contact's information
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                // get the user's email address
                var email: String? = null
                val cursorEmail = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf<String>(id), null)
                if (cursorEmail != null && cursorEmail.moveToFirst()) {
                    email = cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                    cursorEmail.close()
                }

                // get the user's phone number
                var phone: String? = null
                if (hasPhone > 0) {
                    val cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf<String>(id), null)
                    if (cursorPhone != null && cursorPhone.moveToFirst()) {
                        phone = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        cursorPhone.close()
                    }
                }

                // if the user user has an email or phone then add it to contacts
                if ((!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
                        && !email.equals(name, ignoreCase = true)) || !TextUtils.isEmpty(phone)) {
                    val phoneList = ArrayList<String>()
                    val emailList = ArrayList<String>()
                    if (phone != null)
                        phoneList.add(phone)
                    if (email != null)
                        emailList.add(email.toLowerCase())
                    contactList.add(Contact(phoneList, emailList))
                    contactWithNameList.add(ContactWithName(name, phoneList, emailList))
                }

            } while (cursor.moveToNext())

            cursor.close()
        }
    }

    fun updateUserInfoWhenSigningUp(userId: Int?, extraSignUpInfo: ExtraSignUpInfo, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                if (userId != null) {
                    nCardService.updateUserInformation(userId, extraSignUpInfo)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.e(TAG, "update success")
                                successEvent.call()
                            },
                                    { error -> Log.e(TAG, "Unable to update", error) })
                }
            }
        })
    }

    fun updateMyInformation(user: User, successEvent: SingleLiveEvent<Void>, profilePath: String) {
        val userId = uploadFiles(profilePath, user)

        //if updating and no files need to be updated or it is creating new name card without any file
        if (totalFiles == 0 || totalFiles == count) {
            updateUserInfoAfterUpload(user, successEvent)
        } else {
            val observers = transferUtility.getTransfersWithType(TransferType.UPLOAD)
            observers
                    .asSequence()
                    .forEach {
                        if (it.state == TransferState.COMPLETED) {
                            transferUtility.deleteTransferRecord(it.id)
                        } else {
                            it.setTransferListener(object : TransferListener {
                                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                                    Log.e(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                                            id, bytesTotal, bytesCurrent))
                                }

                                override fun onStateChanged(id: Int, state: TransferState?) {
                                    Log.e(TAG, "onStateChanged: $id, $state")
                                    if (state == TransferState.COMPLETED) {
                                        count++
                                        transferUtility.deleteTransferRecord(id)
                                    }
                                    if (count == totalFiles) {
                                        updateUserInfoAfterUpload(user, successEvent)

                                        //clear observers when done
                                        if (observers != null && !observers.isEmpty()) {
                                            for (observerObject in observers) {
                                                observerObject.cleanTransferListener()
                                            }
                                        }
                                    }
                                }

                                override fun onError(id: Int, ex: java.lang.Exception?) {
                                    Log.e(TAG, "Error during upload: " + id, ex)
                                }
                            })
                        }
                    }
        }


    }

    fun updateUserInfoAfterUpload(user: User, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                nCardService.updateMyInformation(user.id, user)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.e(TAG, "update success")
                            successEvent.call()
                            compositeDisposable.add(CompletableFromAction(Action { userDao.updateUser(user) })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Log.e("NCard", "Success update name card table")
                                        compositeDisposable.dispose()
                                    }, { error ->
                                        Log.e("NCard", "Unable to update name card table", error)
                                        compositeDisposable.dispose()
                                    }))
                        },
                                { error -> Log.e(TAG, "Unable to update", error) })
            }
        })
    }

    var totalFiles = 0
    var count = 0
    private fun uploadFiles(profilePath: String, user: User): Int {
        val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        count = 0
        totalFiles = 0

        if (profilePath.isNotEmpty()) {
            totalFiles++
            val file = File(profilePath)
            val key = "userProfile/"
            transferUtility.upload(AppHelper.getCognitoBucketName(), key + file.name, file)
            user.profileImageUrl = Utils.getFilePathInS3NameCard(key, file.name)
        }
        return userId
    }

    fun createFriendRequest(userId: Int, user: User, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                nCardService.createFriendRequest(userId, RequestFriend(user.id))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "send request success")
                                    successEvent.call()
                                    user.status = context.getString(R.string.status_pending)
                                    compositeDisposable.add(CompletableFromAction(Action { userDao.updateUser(user) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success update user table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to update user table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to send", error) })
                // Unblock user from chat
                EventBus.getDefault().post(PrivacyListEvent(user.chatId.toString(), true))
            }
        })
    }

    fun updateFriendRequest(recipientId: Int, initiator: User, successEvent: SingleLiveEvent<Void>, status: String) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                nCardService.updateFriendRequest(initiator.id, recipientId, FriendRequestStatus(status))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "update friend request success")
                                    successEvent.call()
                                    initiator.status = if (status == context.getString(R.string.friend_reject))
                                        context.getString(R.string.status_none) else context.getString(R.string.status_pending)
                                    compositeDisposable.add(CompletableFromAction(Action { userDao.updateUser(initiator) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success update user table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to update user table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to send", error) })
            }
        })
    }

    fun filterUsersByName(filter: UserFilter): LiveData<Resource<List<User>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<User>, List<SearchUserApiResponse>>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: List<SearchUserApiResponse>) {
                val userList = ArrayList<User>()
                userIds.clear()
                item.forEach {
                    it.user.status = it.status
                    it.user.mutualFriendCount = it.mutualFriendCount
                    userList.add(it.user)
                    userIds.add(it.user.id)
                }
                userDao.insertUsers(userList)
            }

            override fun shouldFetch(data: List<User>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<User>> {
                return if (userIds.size == 0) AbsentLiveData.create()
                else userDao.findByUserIds(userIds)
            }

            override fun createCall(): LiveData<ApiResponse<List<SearchUserApiResponse>>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.searchUsers(userId, filter)
            }
        }.asLiveData()
    }

    fun filterUsersByJob(filter: UserFilter): LiveData<Resource<List<User>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<User>, SearchUserByJobApiResponse>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: SearchUserByJobApiResponse) {
                val userList = ArrayList<User>()
                userByJobIds.clear()
                item.users.forEach {
                    it.user.status = it.status
                    it.user.mutualFriendCount = it.mutualFriendCount
                    userList.add(it.user)
                    userByJobIds.add(it.user.id)
                }
                userDao.insertUsers(userList)
            }

            override fun shouldFetch(data: List<User>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<User>> {
                return if (userByJobIds.size == 0) AbsentLiveData.create()
                else userDao.findByUserIds(userByJobIds)
            }

            override fun createCall(): LiveData<ApiResponse<SearchUserByJobApiResponse>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.searchUsersByProfession(userId, ProfessionFiler(filter.name))
            }
        }.asLiveData()
    }

    fun getAllFriendRequests(): LiveData<List<User>> {
        var result: MutableLiveData<List<User>> = MediatorLiveData<List<User>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                val data = nCardService.getAllFriendRequests(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map({ allFriendRequests ->
                            val userList = ArrayList<User>()
                            allFriendRequests.forEach { userList.add(it.initiator) }
                            userList as List<User>
                        })
                data.subscribe({ it ->
                    Log.e(TAG, "getAllFriendRequests")
                    updateNCardInfo(it?.size, null, null, null)
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun getAllFriendRequestsSent(): LiveData<List<User>> {
        var result: MutableLiveData<List<User>> = MediatorLiveData<List<User>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                val data = nCardService.getAllFriendRequestsSent(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map({ allFriendRequests ->
                            val userList = ArrayList<User>()
                            allFriendRequests.forEach { userList.add(it.recipient) }
                            userList as List<User>
                        })
                data.subscribe({ it ->
                    Log.e(TAG, "getAllFriendRequestsSent")
                    updateNCardInfo(null, it?.size, null, null)
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun getAllFriendShared(): LiveData<List<FriendRecommendation>> {
        var result: MutableLiveData<List<FriendRecommendation>> = MediatorLiveData<List<FriendRecommendation>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                val data = nCardService.getAllFriendsShared(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                data.subscribe({ it ->
                    Log.e(TAG, "getAllFriendShared")
                    updateNCardInfo(null, null, it?.size, null)
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun updateFriendSharing(userId: Int?, friendRecommendation: FriendRecommendation, status: String, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                if (userId != null) {
                    nCardService.updateFriendSharing(userId, friendRecommendation.friendSharingId, FriendRequestStatus(status))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.e(TAG, "update friend sharing success")
                                successEvent.call()
                            },
                                    { error -> Log.e(TAG, "Unable to update friend sharing success", error) })
                }
            }
        })
    }

    fun getUserWhenSignIn(userName: String, callback: Callback<User>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                return nCardService.getUserWhenSignIn(userName).enqueue(object : Callback<User> {
                    override fun onFailure(call: Call<User>?, t: Throwable?) {
                        Log.e("NCard", t.toString())
                        callback.onFailure(call, t)
                    }

                    override fun onResponse(call: Call<User>?, response: Response<User>?) {
                        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_USER_ID, response?.body()?.id ?: 0)
                        context.sendBroadcast(Intent(CardsFragment.ID_FETCHED))
                        val compositeDisposable = CompositeDisposable()
                        if (response?.body() != null) {
                            compositeDisposable.add(CompletableFromAction(Action { userDao.insert(response.body()!!) })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        Log.e("NCard", "Insert user success")
                                        compositeDisposable.dispose()
                                    }, { error ->
                                        Log.e("NCard", "Unable to Insert user success", error)
                                        compositeDisposable.dispose()
                                    }))
                        }
                        callback.onResponse(call, response)
                    }
                })
            }
        })
    }

    fun getUserById(userId: Int, user: MutableLiveData<User>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.getUserById(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            appExecutors
                                    .diskIO()
                                    .execute {
                                        db.beginTransaction()
                                        try {
                                            val allFriends = friendDao.loadAllFriends()
                                            run loop@ {
                                                allFriends.forEach {
                                                    if (it.id == response.id) {
                                                        if (response.gender == null) response.gender = ""
                                                        if (response.nationality == null) response.nationality = ""
                                                        if (response.country == null) response.country = ""
                                                        response.status = "friend"

                                                        user.postValue(response)

                                                        return@loop
                                                    }
                                                }
                                            }

                                            db.setTransactionSuccessful()
                                        } finally {
                                            db.endTransaction()
                                        }
                                    }
                            if (user.value == null) {
                                user.value = response
                            }
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    compositeDisposable.dispose()
                                    Log.e("Ncard", "Unable to getUserById", error)
                                })
                )
            }
        })
    }

    fun getMutualFriend(targetId:Int): LiveData<List<Friend>> {
        var result: MutableLiveData<List<Friend>> = MediatorLiveData<List<Friend>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getMutualFriend(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID),targetId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                data.subscribe({ it ->
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun getUserById(id: Int, user: MediatorLiveData<User>) {
        appExecutors
                .mainThread()
                .execute {
                    user.addSource(
                            userDao.findByUserId(id),
                            { newData ->
                                user.setValue(newData)
                            })
                }
    }

    fun deleteUserDB() {
        appExecutors
                .diskIO()
                .execute {
                    // Remove all in db
                    userDao.deleteAllUser()
                    // Remove all ndcardInfo
                    nCardInfoDao.deleteAllNCardInfoDB()
                }
    }

    fun updateNCardInfo(friendRequest: Int?, friendRequestSent: Int?, friendRecommendation: Int?, cardRecommendation: Int?) {
        appExecutors
                .diskIO()
                .execute {
                    //Log.e("NCard", "updateNCardInfo ${friendRequest} ${friendRequestSent} ${friendRecommendation} ${cardRecommendation}")
                    val ncardInfoList: List<NCardInfo> = nCardInfoDao.getNCardInfoList()
                    if (ncardInfoList != null && ncardInfoList.isNotEmpty()) {
                        Log.e("NCard", "updateNCardInfo ncardInfoList != null && ncardInfoList.isNotEmpty() ${friendRequest} ${friendRequestSent} ${friendRecommendation} ${cardRecommendation}")
                        val ncardInfo = ncardInfoList.get(0)
                        if (friendRequest != null) {
                            ncardInfo.friendRequest = friendRequest
                        }
                        if (friendRequestSent != null) {
                            ncardInfo.friendRequestSent = friendRequestSent
                        }
                        if (friendRecommendation != null) {
                            ncardInfo.friendRecommendation = friendRecommendation
                        }
                        if (cardRecommendation != null) {
                            ncardInfo.cardRecommendation = cardRecommendation
                        }
                        Log.e("NCard", "updateNCardInfo ncardInfoList != null  ${ncardInfo.friendRequest} ${ncardInfo.friendRequestSent} ${ncardInfo.friendRecommendation} ${ncardInfo.cardRecommendation}")
                        nCardInfoDao.insert(ncardInfo)
                    } else {
                        Log.e("NCard", "updateNCardInfo ${friendRequest} ${friendRequestSent} ${friendRecommendation} ${cardRecommendation}")
                        val ncardInfo = NCardInfo(friendRequest, friendRequestSent, friendRecommendation, cardRecommendation)
                        nCardInfoDao.insert(ncardInfo)
                    }
                }
    }

    companion object {
        private val TAG = "NCard"
    }
}