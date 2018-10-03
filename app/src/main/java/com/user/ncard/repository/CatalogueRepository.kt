package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import android.util.Log
import com.user.ncard.AppExecutors
import com.user.ncard.api.ApiResponse
import com.user.ncard.api.NCardService
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
class CatalogueRepository @Inject constructor(
        val db: NcardDb,
        val catalogueDao: CatalogueDao,
        val context: Context,
        val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
        val nCardService: NCardService, val appExecutors: AppExecutors,
        val sharedPreferenceHelper: SharedPreferenceHelper) {

    fun getCatalogueTags(): LiveData<Resource<List<TagPost>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<TagPost>, TagsResponse>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: TagsResponse) {
                db.beginTransaction()
                try {
                    catalogueDao.insertTag(item.tags)
                    catalogueDao.insertTagGroup(item.tagGroups)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<TagPost>?): Boolean {
                return data == null || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<TagPost>> {
                return catalogueDao.getAllTags()
            }

            override fun createCall(): LiveData<ApiResponse<TagsResponse>> {
                return nCardService.getCatalogueTags()
            }
        }.asLiveData()
    }

    fun getCatalogueTagGroups(): LiveData<List<TagGroupPost>> {
        return catalogueDao.getAllTagGroups()
    }

    fun createCataloguePost(request: RequestCatalogue): LiveData<Resource<CataloguePost>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)

        return object : NetworkCallResource<CataloguePost>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: CataloguePost) {
                db.beginTransaction()

                try {
                    catalogueDao.insertCataloguePost(item)
                    // Store comments
                    item?.comments?.forEach { comment -> comment.postId = item.id }
                    catalogueDao.insertCatalogueComment(item?.comments)
                    // Store likes
                    item?.likes?.forEach { like -> like.postId = item.id }
                    catalogueDao.insertCatalogueLike(item?.likes)
                    // Insert catalogue screenTracking
                    catalogueDao.insertCataloguePostScreenTracking(CataloguePostScreenTracking(item.id, ScreenTracking.ALL))
                    catalogueDao.insertCataloguePostScreenTracking(CataloguePostScreenTracking(item.id, ScreenTracking.ME))
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<CataloguePost>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.createCataloguePost(userId, request.toLowerCaseReuqest())
            }
        }.asLiveData()
    }

    fun getCataloguePosts(refresh: Boolean,
                          forLoad: Boolean,
                          filterTags: String,
                          filterVisibility: String,
                          postCategory: String,
                          keyword: String,
                          page: Int): LiveData<ResourcePaging<List<CatalogueContainer>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResourcePaging<List<CatalogueContainer>, CataloguePostResponse>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: CataloguePostResponse) {
                db.beginTransaction()
                try {
                    if (refresh) {
                        // Remove all in db
                        val listCataloguePostScreenTracking = catalogueDao.getCataloguePostScreenTracking(ScreenTracking.ALL)
                        var listSuitablePostId: MutableList<Int> = ArrayList<Int>()
                        (listCataloguePostScreenTracking != null && listCataloguePostScreenTracking.isNotEmpty()).let {
                            listCataloguePostScreenTracking.forEach {
                                listSuitablePostId.add(it.postId)
                            }
                            if (listSuitablePostId.isNotEmpty())
                                catalogueDao.deleteCataloguePost(listSuitablePostId.toTypedArray())
                        }
                    }
                    catalogueDao.insertCataloguePost(item?.posts)
                    item?.posts.forEach { catalogue ->
                        // Store comments
                        catalogue?.comments?.forEach { comment -> comment.postId = catalogue.id }
                        catalogueDao.insertCatalogueComment(catalogue?.comments)
                        // Store likes
                        catalogue?.likes?.forEach { like -> like.postId = catalogue.id }
                        catalogueDao.insertCatalogueLike(catalogue?.likes)
                        // Insert catalogue screenTracking
                        catalogueDao.insertCataloguePostScreenTracking(CataloguePostScreenTracking(catalogue.id, ScreenTracking.ALL))
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun returnPaging(item: CataloguePostResponse): Pagination {
                return item?.pagination
            }


            override fun shouldFetch(data: List<CatalogueContainer>?): Boolean {
                return data == null || forLoad || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<CatalogueContainer>> {
                return catalogueDao.getAllCataloguePostContainers(ScreenTracking.ALL, postCategory.toLowerCase())
            }

            override fun createCall(): LiveData<ApiResponse<CataloguePostResponse>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.getCataloguePosts(userId, filterTags,
                        if (filterVisibility.toLowerCase() == SharePost.SharePostType.FRIENDS.type.toLowerCase()) "friend" else filterVisibility.toLowerCase(),
                        postCategory.toLowerCase(), keyword, page)
            }
        }.asLiveData()
    }


    fun getMyCataloguePosts(refresh: Boolean,
                            forLoad: Boolean,
                            postCategory: String,
                            page: Int): LiveData<ResourcePaging<List<CatalogueContainer>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResourcePaging<List<CatalogueContainer>, CataloguePostResponse>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: CataloguePostResponse) {
                db.beginTransaction()
                try {
                    if (refresh) {
                        // Remove all in db
                        val listCataloguePostScreenTracking = catalogueDao.getCataloguePostScreenTracking(ScreenTracking.ME)
                        var listSuitablePostId: MutableList<Int> = ArrayList<Int>()
                        (listCataloguePostScreenTracking != null && listCataloguePostScreenTracking.isNotEmpty()).let {
                            listCataloguePostScreenTracking.forEach {
                                listSuitablePostId.add(it.postId)
                            }
                            if (listSuitablePostId.isNotEmpty())
                                catalogueDao.deleteCataloguePost(listSuitablePostId.toTypedArray())
                        }
                    }
                    catalogueDao.insertCataloguePost(item?.posts)
                    item?.posts.forEach { catalogue ->
                        // Store comments
                        catalogue?.comments?.forEach { comment -> comment.postId = catalogue.id }
                        catalogueDao.insertCatalogueComment(catalogue?.comments)
                        // Store likes
                        catalogue?.likes?.forEach { like -> like.postId = catalogue.id }
                        catalogueDao.insertCatalogueLike(catalogue?.likes)
                        // Insert catalogue screenTracking
                        catalogueDao.insertCataloguePostScreenTracking(CataloguePostScreenTracking(catalogue.id, ScreenTracking.ME))
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun returnPaging(item: CataloguePostResponse): Pagination {
                return item?.pagination
            }


            override fun shouldFetch(data: List<CatalogueContainer>?): Boolean {
                return data == null || forLoad || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<CatalogueContainer>> {
                return catalogueDao.getAllCataloguePostContainers(ScreenTracking.ME, postCategory.toLowerCase())
            }

            override fun createCall(): LiveData<ApiResponse<CataloguePostResponse>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.getMyCataloguePosts(userId, postCategory.toLowerCase(), page)
            }
        }.asLiveData()
    }


    fun getCataloguePostDetail(refresh: Boolean,
                               forLoad: Boolean, postId: Int,
                               page: Int): LiveData<Resource<CatalogueContainer>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<CatalogueContainer, CataloguePost>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: CataloguePost) {
                db.beginTransaction()
                try {
                    catalogueDao.insertCataloguePost(item)
                    // Store comments
                    item?.comments?.forEach { comment -> comment.postId = item.id }
                    catalogueDao.insertCatalogueComment(item?.comments)
                    // Store likes
                    item?.likes?.forEach { like -> like.postId = item.id }
                    catalogueDao.insertCatalogueLike(item?.likes)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: CatalogueContainer?): Boolean {
                return data == null || forLoad
            }

            override fun loadFromDb(): LiveData<CatalogueContainer> {
                return catalogueDao.getCataloguePostContainer(postId)
            }

            override fun createCall(): LiveData<ApiResponse<CataloguePost>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.getDetailCataloguePost(userId, postId)
            }
        }.asLiveData()
    }

    fun deleteCataloguePost(postId: Int): LiveData<Resource<MessageObject>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)

        return object : NetworkCallResource<MessageObject>(appExecutors, sharedPreferenceHelper) {
            override fun returnCallResult(item: MessageObject) {
                db.beginTransaction()

                try {
                    catalogueDao.deleteCataloguePostById(postId)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<MessageObject>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.deleteCataloguePost(userId, postId)
            }
        }.asLiveData()
    }


    fun createCommentCataloguePost(postId: Int, request: RequestComment): LiveData<Resource<CataloguePost>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkCallResource<CataloguePost>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: CataloguePost) {
                db.beginTransaction()

                try {
                    catalogueDao.insertCataloguePost(item)
                    // Store comments
                    item?.comments?.forEach { comment -> comment.postId = item.id }
                    catalogueDao.insertCatalogueComment(item?.comments)
                    // Store likes
                    item?.likes?.forEach { like -> like.postId = item.id }
                    catalogueDao.insertCatalogueLike(item?.likes)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<CataloguePost>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.createCommentCataloguePost(userId, postId, request)
            }
        }.asLiveData()
    }

    fun likeCataloguePost(postId: Int, request: RequestLike): LiveData<Resource<CataloguePost>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkCallResource<CataloguePost>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: CataloguePost) {
                db.beginTransaction()

                try {
                    catalogueDao.insertCataloguePost(item)
                    // Store comments
                    item?.comments?.forEach { comment -> comment.postId = item.id }
                    catalogueDao.insertCatalogueComment(item?.comments)
                    // Store likes
                    item?.likes?.forEach { like -> like.postId = item.id }
                    catalogueDao.insertCatalogueLike(item?.likes)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<CataloguePost>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.likeCataloguePost(userId, postId, request)
            }
        }.asLiveData()
    }

    fun deleteCataloguePostDB() {
        appExecutors
                .diskIO()
                .execute {
                    // Remove all in db
                    catalogueDao.deleteAllCatalogue()
                    catalogueDao.deleteAllCataloguePostScreenTracking()
                }
    }


}