package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.user.ncard.AppExecutors
import com.user.ncard.SingleLiveEvent
import com.user.ncard.api.ApiResponse
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.api.NCardService
import com.user.ncard.db.FilterDao
import com.user.ncard.db.FriendDao
import com.user.ncard.db.NcardDb
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class FriendRepository @Inject constructor(val friendDao: FriendDao, val filterDao: FilterDao, val db: NcardDb,
                                           val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                           val nCardService: NCardService, val appExecutors: AppExecutors,
                                           val sharedPreferenceHelper: SharedPreferenceHelper) {
    fun getALlFriends(): LiveData<Resource<List<Friend>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<Friend>, List<Friend>>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: List<Friend>) {
                db.beginTransaction()
                try {
                    friendDao.deleteAllFriends()
                    filterDao.deleteAllFiltersOfFriend()
                    item.forEach {
                        if (it.gender == null) it.gender = ""
                        if (it.nationality == null) it.nationality = ""
                        if (it.country == null) it.country = ""
                        it.status = "friend"

                        if (!it.country.isNullOrEmpty()) filterDao.insert(Filter(it.country!!, TYPE_COUNTRY, PARENT))
                        if (!it.nationality.isNullOrEmpty()) filterDao.insert(Filter(it.nationality!!, TYPE_NATIONALITY, PARENT))
                        if (it.jobs != null) {
                            it.jobs!!.forEach { if (!it?.industry.isNullOrEmpty()) filterDao.insert(Filter(it?.industry!!, TYPE_INDUSTRY, PARENT)) }
                        }
                    }
                    friendDao.insertUsers(item)

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Friend>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Friend>> {

                return friendDao.findAllFriends()
            }

            override fun createCall(): LiveData<ApiResponse<List<Friend>>> {
                return nCardService.getAllFriends(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
            }
        }.asLiveData()
    }

    fun regetALlFriends() {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.regetAllFriends(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ item ->
                            appExecutors
                                    .diskIO()
                                    .execute {
                                        db.beginTransaction()
                                        try {
                                            friendDao.deleteAllFriends()
                                            filterDao.deleteAllFiltersOfFriend()
                                            item.forEach {
                                                if (it.gender == null) it.gender = ""
                                                if (it.nationality == null) it.nationality = ""
                                                if (it.country == null) it.country = ""
                                                it.status = "friend"

                                                if (!it.country.isNullOrEmpty()) filterDao.insert(Filter(it.country!!, TYPE_COUNTRY, PARENT))
                                                if (!it.nationality.isNullOrEmpty()) filterDao.insert(Filter(it.nationality!!, TYPE_NATIONALITY, PARENT))
                                                if (it.jobs != null) {
                                                    it.jobs!!.forEach { if (!it?.industry.isNullOrEmpty()) filterDao.insert(Filter(it?.industry!!, TYPE_INDUSTRY, PARENT)) }
                                                }
                                            }
                                            friendDao.insertUsers(item)

                                            db.setTransactionSuccessful()
                                        } finally {
                                            db.endTransaction()
                                        }
                                    }
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    Log.e("Ndcard", "Unable to get response", error)
                                    compositeDisposable.dispose()
                                })
                )
            }
        })
    }

    fun getFriendsWithFilter(friends: MediatorLiveData<List<Friend>>, userFilter: UserFilter) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                appExecutors
                        .mainThread()
                        .execute {
                            friends.addSource(
                                    friendDao.findFriendsWithFilter("%${userFilter.name}%", userFilter.nationality, userFilter.country, userFilter.gender),
                                    { newData ->
                                        friends.setValue(newData?.filter { friend ->
                                            if (userFilter.industry.contains("")) true else !Collections.disjoint(userFilter.industry, friend.jobs?.map {
                                                job -> job?.industry
                                            })
                                        })
                                    })
                        }
            }
        })
    }

    fun getFriendsWithoutFilter(friends: MediatorLiveData<List<Friend>>, name: String) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                appExecutors
                        .mainThread()
                        .execute {
                            friends.addSource(
                                    friendDao.findFriendsWithoutFilter("%$name%"),
                                    { newData ->
                                        friends.setValue(newData)
                                    })
                        }
            }
        })
    }

    fun getFriendsFromDb(friends: MediatorLiveData<List<Friend>>) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                appExecutors
                        .mainThread()
                        .execute {
                            friends.addSource(
                                    friendDao.findAllFriends(),
                                    { newData ->
                                        friends.setValue(newData)
                                    })
                        }
            }
        })
    }

    fun getFriendsByIdFromDb(ids: List<Int>, friends: MediatorLiveData<List<Friend>>) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                appExecutors
                        .mainThread()
                        .execute {
                            friends.addSource(
                                    friendDao.findFriendsById(ids),
                                    { newData ->
                                        friends.setValue(newData)
                                    })
                        }
            }
        })
    }

    fun updateFriendRemark(userId: Int, friend: BaseEntity, successEvent: SingleLiveEvent<Void>, remark: String) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                nCardService.updateFriendRemark(userId, friend.id, FriendRemark(remark))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "update friend remark success")
                                    successEvent.call()
                                    friend.remark = remark
                                    CompletableFromAction(Action { friendDao.updateFriend(friend as Friend) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success update friend table")
                                            }, { error ->
                                                Log.e("NCard", "Unable to update friend table", error)
                                            }).dispose()
                                },
                                { error -> Log.e(TAG, "Unable to send", error) })
            }
        })
    }

    fun deleteFriend(userId: Int, friend: BaseEntity, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                nCardService.deleteFriend(userId, friend.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "delete friend success")
                                    successEvent.call()
                                    compositeDisposable.add(CompletableFromAction(Action { friendDao.deleteFriendById(friend.id) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success delete friend from db")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to delete friend from db", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to send", error) })
            }
        })

    }

    fun shareFriend(userId: Int?, friend: BaseEntity, to: ArrayList<Int>, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
                if (userId != null) {
                    nCardService.shareFriend(userId, to.map { it -> FriendSharing(it, friend.id) })
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

    fun deleteFriendDB() {
        appExecutors
                .diskIO()
                .execute {
                    // Remove all in db
                    friendDao.deleteAllFriends()
                }
    }

    companion object {
        private val TAG = "NCard"
        private const val TYPE_COUNTRY = "country"
        private const val TYPE_INDUSTRY = "industry"
        private const val TYPE_NATIONALITY = "nationality"
        private const val PARENT = "friend"
    }
}