package com.user.ncard.repository

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Environment
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.user.ncard.AppExecutors
import com.user.ncard.SingleLiveEvent
import com.user.ncard.api.ApiResponse
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.api.NCardService
import com.user.ncard.db.FilterDao
import com.user.ncard.db.NCardInfoDao
import com.user.ncard.db.NameCardDao
import com.user.ncard.db.NcardDb
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.util.AppHelper
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class NameCardRepository @Inject constructor(val nameCardDao: NameCardDao, val nCardInfoDao: NCardInfoDao, val context: Context, val db: NcardDb,
                                             val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                             val nCardService: NCardService, val transferUtility: TransferUtility,
                                             val sharedPreferenceHelper: SharedPreferenceHelper,
                                             val appExecutors: AppExecutors, val filterDao: FilterDao) {

    var totalFiles = 0
    var count = 0
    fun createNameCard(nameCard: NameCard?, createNameCardSuccess: SingleLiveEvent<Void>,
                       profilePath: String, mediaFilePathsList: ArrayList<String>,
                       logoFilePathsList: ArrayList<String>, isMyNameCard: Boolean, frontPath: String, backPath: String) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val userId = uploadFiles(profilePath, nameCard, mediaFilePathsList, logoFilePathsList, frontPath, backPath)

                //if updating and no files need to be updated or it is creating new name card without any file
                if (totalFiles == 0 || totalFiles == count) {
                    createNameCardAfterUpload(userId, nameCard, createNameCardSuccess, isMyNameCard)
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
                                                createNameCardAfterUpload(userId, nameCard, createNameCardSuccess, isMyNameCard)

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
        })
    }

    private fun createNameCardAfterUpload(userId: Int, nameCard: NameCard?, createNameCardSuccess: SingleLiveEvent<Void>, isMyNameCard: Boolean) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                if (nameCard != null) {
                    if (isMyNameCard) {
                        nCardService.createNameCardForAJob(userId, nameCard.jobId!!, nameCard)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        {
                                            Log.e(TAG, "create name card success")
                                            createNameCardSuccess.call()
                                            compositeDisposable.add(CompletableFromAction(Action { nameCardDao.insert(nameCard) })
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe({
                                                        Log.e("NCard", "Success insert name card table")
                                                        compositeDisposable.dispose()
                                                    }, { error ->
                                                        Log.e("NCard", "Unable to insert name card table", error)
                                                        compositeDisposable.dispose()
                                                    }))
                                        },
                                        { error -> Log.e(TAG, "Unable to create", error) })
                    } else {
                        nCardService.createNameCardForUser(userId, nameCard)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        {
                                            Log.e(TAG, "create name card success")
                                            createNameCardSuccess.call()
                                            compositeDisposable.add(CompletableFromAction(Action { nameCardDao.insert(nameCard) })
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe({
                                                        Log.e("NCard", "Success insert name card table")
                                                        compositeDisposable.dispose()
                                                    }, { error ->
                                                        Log.e("NCard", "Unable to insert name card table", error)
                                                        compositeDisposable.dispose()
                                                    }))
                                        },
                                        { error -> Log.e(TAG, "Unable to create", error) })
                    }
                }
            }
        })
    }

    fun updateNameCard(nameCard: NameCard?, createNameCardSuccess: SingleLiveEvent<Void>,
                       profilePath: String, mediaFilePathsList: ArrayList<String>,
                       logoFilePathsList: ArrayList<String>, isMyNameCard: Boolean, frontPath: String, backPath: String) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val userId = uploadFiles(profilePath, nameCard, mediaFilePathsList, logoFilePathsList, frontPath, backPath)
                //if updating and no files need to be updated or it is creating new name card without any file
                if (totalFiles == 0 || totalFiles == count) {
                    updateNameCardAfterUpload(userId, nameCard, createNameCardSuccess, isMyNameCard)
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
                                                updateNameCardAfterUpload(userId, nameCard, createNameCardSuccess, isMyNameCard)

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
        })
    }

    private fun updateNameCardAfterUpload(userId: Int, nameCard: NameCard?, createNameCardSuccess: SingleLiveEvent<Void>, isMyNameCard: Boolean) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                if (nameCard != null) {
                    if (isMyNameCard) {
                        nCardService.updateNameCardForAJob(userId, nameCard.jobId!!, nameCard)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        {
                                            Log.e(TAG, "update name card success")
                                            createNameCardSuccess.call()
                                            compositeDisposable.add(CompletableFromAction(Action { nameCardDao.updateNameCard(nameCard) })
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
                    } else {
                        nCardService.updateNameCardForUser(userId, nameCard.id, nameCard)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        {
                                            Log.e(TAG, "update name card success")
                                            createNameCardSuccess.call()
                                            compositeDisposable.add(CompletableFromAction(Action { nameCardDao.updateNameCard(nameCard) })
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
                }
            }
        })
    }

    fun updateNameCardRemark(userId: Int, nameCard: NameCard, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                nCardService.updateNameCardForUser(userId, nameCard.id, nameCard)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "update nameCard remark success")
                                    successEvent.call()
                                    //nameCard.remark = remark
                                    compositeDisposable.add(CompletableFromAction(Action { nameCardDao.updateNameCard(nameCard) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success update nameCard table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to update nameCard table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to send", error) })
            }
        })
    }

    private fun uploadFiles(profilePath: String, nameCard: NameCard?, mediaFilePathsList: ArrayList<String>,
                            logoFilePathsList: ArrayList<String>, frontPath: String, backPath: String): Int {
        val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
        changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
        count = 0
        totalFiles = 0
        val key = "nameCard/"
        if (profilePath.isNotEmpty()) {
            totalFiles++
            val file = File(profilePath)
            transferUtility.upload(AppHelper.getCognitoBucketName(), key + file.name, file)
            nameCard?.companyLogoUrl = Utils.getFilePathInS3NameCard(key, file.name)
        }
        if (frontPath.isNotEmpty()) {
            totalFiles++
            val file = File(frontPath)
            transferUtility.upload(AppHelper.getCognitoBucketName(), "nameCard/" + file.name, file)
            nameCard?.frontUrl = Utils.getFilePathInS3NameCard(key, file.name)
        }
        if (backPath.isNotEmpty()) {
            totalFiles++
            val file = File(backPath)
            transferUtility.upload(AppHelper.getCognitoBucketName(), "nameCard/" + file.name, file)
            nameCard?.backUrl = Utils.getFilePathInS3NameCard(key, file.name)
        }
        if (mediaFilePathsList.isNotEmpty()) {
            totalFiles += mediaFilePathsList.size
            nameCard?.mediaUrls = ArrayList()
            mediaFilePathsList.forEach {
                if (it.contains("http")) {
                    nameCard?.mediaUrls?.add(it)
                    count++
                } else {
                    val file = File(it)
                    transferUtility.upload(AppHelper.getCognitoBucketName(), "nameCard/" + file.name, file)
                    nameCard?.mediaUrls?.add(Utils.getFilePathInS3NameCard(key, file.name))
                }
            }
        }

        if (logoFilePathsList.isNotEmpty()) {
            totalFiles += logoFilePathsList.size
            nameCard?.certUrls = ArrayList()
            logoFilePathsList.forEach {
                if (it.contains("http")) {
                    nameCard?.certUrls?.add(it)
                    count++
                } else {
                    val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + "drawable" + UUID.randomUUID().toString() + ".png")
                    val inputStream = context.resources.openRawResource(it.toInt())
                    val out = FileOutputStream(file)
                    val buf = ByteArray(1024)
                    var len: Int
                    len = inputStream.read(buf)
                    while (len > 0) {
                        out.write(buf, 0, len)
                        len = inputStream.read(buf)
                    }
                    out.close()
                    inputStream.close()
                    transferUtility.upload(AppHelper.getCognitoBucketName(), "nameCard/" + file.name, file)
                    nameCard?.certUrls?.add(Utils.getFilePathInS3NameCard(key, file.name))
                }
            }
        }
        return userId
    }

    fun getALlNameCards(): LiveData<Resource<List<NameCard>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<NameCard>, List<NameCard>>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: List<NameCard>) {
                db.beginTransaction()
                try {
                    nameCardDao.deleteAllNameCards()
                    nameCardDao.insertNameCards(item)
                    filterDao.deleteAllFiltersOfNameCard()
                    item.forEach {
                        if (!it.country.isNullOrEmpty()) filterDao.insert(Filter(it.country!!, TYPE_COUNTRY, PARENT))
                        if (!it.nationality.isNullOrEmpty()) filterDao.insert(Filter(it.nationality!!, TYPE_NATIONALITY, PARENT))
                        if (!it.industry.isNullOrEmpty()) filterDao.insert(Filter(it.industry!!, TYPE_INDUSTRY, PARENT))
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<NameCard>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<NameCard>> {

                return nameCardDao.findAllNameCards()
            }

            override fun createCall(): LiveData<ApiResponse<List<NameCard>>> {
                return nCardService.getAllNameCards(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
            }
        }.asLiveData()
    }

    fun deleteNameCard(userId: Int, nameCard: NameCard, successEvent: SingleLiveEvent<Void>, isMyNameCard: Boolean) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                if (isMyNameCard) {
                    nCardService.deleteNameCardForAJob(userId, nameCard.jobId!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {
                                        Log.e(TAG, "delete nameCard success")
                                        successEvent.call()
                                        compositeDisposable.add(CompletableFromAction(Action { nameCardDao.deleteNameCardById(nameCard.id) })
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe({
                                                    Log.e("NCard", "Success delete nameCard from db")
                                                    compositeDisposable.dispose()
                                                }, { error ->
                                                    Log.e("NCard", "Unable to delete nameCard from db", error)
                                                    compositeDisposable.dispose()
                                                }))
                                    },
                                    { error -> Log.e(TAG, "Unable to send", error) })
                } else {
                    nCardService.deleteNameCard(userId, nameCard.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    {
                                        Log.e(TAG, "delete nameCard success")
                                        successEvent.call()
                                        compositeDisposable.add(CompletableFromAction(Action { nameCardDao.deleteNameCardById(nameCard.id) })
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe({
                                                    Log.e("NCard", "Success delete nameCard from db")
                                                    compositeDisposable.dispose()
                                                }, { error ->
                                                    Log.e("NCard", "Unable to delete nameCard from db", error)
                                                    compositeDisposable.dispose()
                                                }))
                                    },
                                    { error -> Log.e(TAG, "Unable to send", error) })
                }
            }
        })
    }

    fun getNameCardsWithFilter(nameCards: MediatorLiveData<List<NameCard>>, userFilter: UserFilter) {
        appExecutors
                .mainThread()
                .execute {
                    nameCards.addSource(
                            nameCardDao.findNameCardsWithFilter("%${userFilter.name}%", userFilter.nationality, userFilter.country, userFilter.gender, userFilter.industry),
                            { newData ->
                                nameCards.setValue(newData)
                            })
                }
    }

    fun getNameCardsWithoutFilter(nameCards: MediatorLiveData<List<NameCard>>, name: String) {
        appExecutors
                .mainThread()
                .execute {
                    nameCards.addSource(
                            nameCardDao.findNameCardsWithoutFilter("%$name%"),
                            { newData ->
                                nameCards.setValue(newData)
                            })
                }
    }

    fun getNameCardsFromDb(nameCards: MediatorLiveData<List<NameCard>>) {
        appExecutors
                .mainThread()
                .execute {
                    nameCards.addSource(
                            nameCardDao.findAllNameCards(),
                            { newData ->
                                nameCards.setValue(newData)
                            })
                }
    }

    fun getNameCardsByIdFromDb(ids: List<Int>, nameCards: MediatorLiveData<List<NameCard>>) {
        appExecutors
                .mainThread()
                .execute {
                    nameCards.addSource(
                            nameCardDao.findNameCardsById(ids),
                            { newData ->
                                nameCards.setValue(newData)
                            })
                }
    }

    fun getAllMyNameCards(): LiveData<Resource<List<NameCard>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<NameCard>, List<NameCard>>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: List<NameCard>) {
                db.beginTransaction()
                try {
                    nameCardDao.deleteAllMyNameCards()
                    nameCardDao.insertNameCards(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<NameCard>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<NameCard>> {

                return nameCardDao.findAllMyNameCards()
            }

            override fun createCall(): LiveData<ApiResponse<List<NameCard>>> {
                return nCardService.getAllMyNameCard(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
            }
        }.asLiveData()
    }

    fun shareNameCard(userId: Int?, nameCard: NameCard, to: ArrayList<Int>, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                if (userId != null) {
                    nCardService.shareNameCard(userId, to.map { it -> NameCardSharing(it, nameCard.id) })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.e(TAG, "share success")
                                successEvent.call()
                            },
                                    { error -> Log.e(TAG, "Unable to share", error) })
                }
            }
        })
    }

    fun getAllNameCardShared(): LiveData<List<NameCardRecommendation>> {
        var result: MutableLiveData<List<NameCardRecommendation>> = MediatorLiveData<List<NameCardRecommendation>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                val data = nCardService.getAllNameCardsShared(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                data.subscribe({ it ->
                    Log.e(TAG, "getAllNameCardShared")
                    updateNCardInfo(null, null, null, it?.size)
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    @SuppressLint("CheckResult")
    fun updateNameCardSharing(userId: Int?, nameCardRecommendation: NameCardRecommendation, status: String, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                if (userId != null) {
                    nCardService.updateNameCardSharing(userId, nameCardRecommendation.nameCardSharingId, FriendRequestStatus(status))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Log.e(TAG, "update name card sharing success")
                                successEvent.call()
                                if (status == "accept") {
                                    compositeDisposable.add(CompletableFromAction(Action { nameCardDao.insert(nameCardRecommendation.suggestedNameCard) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success insert nameCard from db")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to insert nameCard to db", error)
                                                compositeDisposable.dispose()
                                            }))
                                }
                            },
                                    { error -> Log.e(TAG, "Unable to update name card sharing success", error) })
                }
            }
        })
    }

    fun deleteNameCardDB() {
        appExecutors
                .diskIO()
                .execute {
                    // Remove all in db
                    nameCardDao.deleteAllNameCardsDB()
                }
    }

    fun updateNCardInfo(friendRequest: Int?, friendRequestSent: Int?, friendRecommendation: Int?, cardRecommendation: Int?) {
        appExecutors
                .diskIO()
                .execute {
                    db.beginTransaction()
                    try {
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
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
    }

    companion object {
        private val TAG = "NCard"
        private const val TYPE_COUNTRY = "country"
        private const val TYPE_INDUSTRY = "industry"
        private const val TYPE_NATIONALITY = "nationality"
        private const val PARENT = "namecard"
    }
}