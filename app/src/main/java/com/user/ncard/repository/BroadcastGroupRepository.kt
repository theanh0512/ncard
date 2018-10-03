package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import com.user.ncard.AppExecutors
import com.user.ncard.api.ApiResponse
import com.user.ncard.api.NCardService
import com.user.ncard.db.BroadcastGroupDao
import com.user.ncard.db.CatalogueDao
import com.user.ncard.db.NcardDb
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.ui.catalogue.*
import com.user.ncard.ui.catalogue.utils.NetworkBoundResourcePaging
import com.user.ncard.ui.catalogue.utils.NetworkCallResource
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/10/17.
 */
class BroadcastGroupRepository @Inject constructor(
        val db: NcardDb,
        val broadcastGroupDao: BroadcastGroupDao,
        val context: Context,
        val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
        val nCardService: NCardService, val appExecutors: AppExecutors,
        val sharedPreferenceHelper: SharedPreferenceHelper) {

    fun createBroadcastGroup(request: BroadcastGroup): LiveData<Resource<BroadcastGroup>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)

        return object : NetworkCallResource<BroadcastGroup>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: BroadcastGroup) {
                db.beginTransaction()

                try {
                    broadcastGroupDao.insertBroadcastGroupChat(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<BroadcastGroup>> {
                return nCardService.createBroadcastGroup(request)
            }
        }.asLiveData()
    }

    fun getAllBroadcastGroups(refresh: Boolean,
                              forLoad: Boolean,
                              page: Int): LiveData<ResourcePaging<List<BroadcastGroup>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResourcePaging<List<BroadcastGroup>, List<BroadcastGroup>>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: List<BroadcastGroup>) {
                db.beginTransaction()
                try {
                    if (refresh) {
                        // Remove all in db
                        broadcastGroupDao.deleteAllBroadcastGroup()
                    }
                    broadcastGroupDao.insertBroadcastGroupChat(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun returnPaging(item: List<BroadcastGroup>): Pagination? {
                return null
            }


            override fun shouldFetch(data: List<BroadcastGroup>?): Boolean {
                return data == null || forLoad || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<BroadcastGroup>> {
                return broadcastGroupDao.getAllBroadcastGroupChat()
            }

            override fun createCall(): LiveData<ApiResponse<List<BroadcastGroup>>> {
                return nCardService.getAllBroadcastGroups()
            }
        }.asLiveData()
    }

    fun getBroadcastGroupDetail(refresh: Boolean,
                                forLoad: Boolean, groupId: Int): LiveData<Resource<BroadcastGroup>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<BroadcastGroup, BroadcastGroup>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: BroadcastGroup) {
                db.beginTransaction()
                try {
                    broadcastGroupDao.insertBroadcastGroupChat(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: BroadcastGroup?): Boolean {
                return data == null || forLoad
            }

            override fun loadFromDb(): LiveData<BroadcastGroup> {
                return broadcastGroupDao.getBroadcastGroup(groupId)
            }

            override fun createCall(): LiveData<ApiResponse<BroadcastGroup>> {
                return nCardService.getBroadcastGroupDetail(groupId)
            }
        }.asLiveData()
    }

    fun deleteCataloguePost(groupId: Int): LiveData<Resource<MessageObject>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)

        return object : NetworkCallResource<MessageObject>(appExecutors, sharedPreferenceHelper) {
            override fun returnCallResult(item: MessageObject) {
                db.beginTransaction()

                try {
                    broadcastGroupDao.deleteBroadcastGroup(groupId)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<MessageObject>> {
                return nCardService.deleteBroadcastGroup(groupId)
            }
        }.asLiveData()
    }

    fun updateBroadcastGroup(groupId: Int, request: BroadcastGroup): LiveData<Resource<MessageObject>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)

        return object : NetworkCallResource<MessageObject>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: MessageObject) {
                db.beginTransaction()

                try {

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<MessageObject>> {
                return nCardService.updateBroadcastGroup(groupId, request)
            }
        }.asLiveData()
    }
}