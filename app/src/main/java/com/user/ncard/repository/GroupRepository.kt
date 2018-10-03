package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.user.ncard.AppExecutors
import com.user.ncard.SingleLiveEvent
import com.user.ncard.api.ApiResponse
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.api.NCardService
import com.user.ncard.db.GroupDao
import com.user.ncard.db.NcardDb
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.Group
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.RequestGroup
import com.user.ncard.vo.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class GroupRepository @Inject constructor(val groupDao: GroupDao, val db: NcardDb,
                                          val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                          val appExecutors: AppExecutors, val nCardService: NCardService,
                                          val sharedPreferenceHelper: SharedPreferenceHelper) {

    fun createGroup(group: Group, createGroupSuccess: SingleLiveEvent<Void>, groupLiveData: MutableLiveData<Group>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val memberIds = ArrayList<Int>()
                val members = if (group.members?.isEmpty() != false) null else {
                    group.members?.forEach {
                        if (it != null) memberIds.add(it.id)
                    }
                    memberIds
                }
                val nameCardIds = ArrayList<Int>()
                val nameCards = if (group.nameCards?.isEmpty() != false) null else {
                    group.nameCards?.forEach {
                        if (it != null) nameCardIds.add(it.id)
                    }
                    nameCardIds
                }
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                val compositeDisposable = CompositeDisposable()
                nCardService.createGroupForUser(userId, RequestGroup(group.name, members, nameCards))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { groupData ->
                                    Log.e(TAG, "create group success")
                                    createGroupSuccess.call()
                                    groupLiveData.value = groupData
                                    compositeDisposable.add(CompletableFromAction(Action { groupDao.insert(groupData) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success group card table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to insert group table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to create", error) })
            }
        })
    }

    fun updateGroup(group: Group, updateSuccess: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val memberIds = ArrayList<Int>()
                val members = if (group.members?.isEmpty() != false) memberIds else {
                    group.members?.forEach {
                        if (it != null) memberIds.add(it.id)
                    }
                    memberIds
                }
                val nameCardIds = ArrayList<Int>()
                val nameCards = if (group.nameCards?.isEmpty() != false) nameCardIds else {
                    group.nameCards?.forEach {
                        if (it != null) nameCardIds.add(it.id)
                    }
                    nameCardIds
                }
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                val compositeDisposable = CompositeDisposable()
                nCardService.updateGroupForUser(userId, group.id, RequestGroup(group.name, members, nameCards))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "update group success")
                                    updateSuccess.call()
                                    compositeDisposable.add(CompletableFromAction(Action { groupDao.insert(group) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success group card table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to update group table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to update", error) })
            }
        })
    }

    fun getALlGroups(): LiveData<Resource<List<Group>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<Group>, List<Group>>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: List<Group>) {
                db.beginTransaction()
                try {
                    groupDao.deleteAllGroups()
                    groupDao.insertGroups(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Group>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Group>> {

                return groupDao.findAllGroups()
            }

            override fun createCall(): LiveData<ApiResponse<List<Group>>> {
                return nCardService.getAllGroups(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
            }
        }.asLiveData()
    }

    fun deleteGroup(userId: Int, groupId: Int, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                nCardService.deleteGroup(userId, groupId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "delete group success")
                                    successEvent.call()
                                    compositeDisposable.add(CompletableFromAction(Action { groupDao.deleteGroupById(groupId) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success delete group from db")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to delete group from db", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to send", error) })
            }
        })
    }

    companion object {
        private val TAG = "NCard"
    }
}