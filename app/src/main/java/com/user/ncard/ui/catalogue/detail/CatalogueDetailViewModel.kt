package com.user.ncard.ui.catalogue.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.util.Log
import com.user.ncard.repository.CatalogueRepository
import com.user.ncard.ui.catalogue.*
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.CatalogueComment
import com.user.ncard.vo.CatalogueContainer
import com.user.ncard.vo.CataloguePost
import com.user.ncard.vo.Resource
import java.util.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 23/10/17.
 */
class CatalogueDetailViewModel @Inject constructor(val catalogueRepository: CatalogueRepository,
                                                   val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {

    lateinit var catalogueLike: LiveData<Resource<CataloguePost>>
    lateinit var catalogueComment: LiveData<Resource<CataloguePost>>
    var postId = MutableLiveData<Int>()
    val delete = MutableLiveData<Boolean>()
    lateinit var catalogue: LiveData<Resource<CatalogueContainer>>
    lateinit var deleteCatalogue: LiveData<Resource<MessageObject>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    var id: Int? = -1

    init {
        forceLoad = false
        initData()
    }

    override fun initData() {
        catalogue = Transformations.switchMap(postId) { postId ->
            if (postId == null) {
                return@switchMap AbsentLiveData.create<Resource<CatalogueContainer>>()
            }
            isLoading = true
            return@switchMap catalogueRepository.getCataloguePostDetail(refresh, forceLoad, postId, page)
        }

        deleteCatalogue = Transformations.switchMap(delete) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<MessageObject>>()
            }
            return@switchMap catalogueRepository.deleteCataloguePost(postId.value!!)
        }
    }

    fun init(postId: Int?) {
        this.id = postId

        initData()
    }

    fun getItems(): List<CatalogueContainer> {
        if (catalogue != null && catalogue?.value != null && catalogue?.value?.data != null) {
            return arrayListOf(catalogue?.value?.data!!)

        }
        return Collections.emptyList<CatalogueContainer>()
    }

    fun getCommentItems(): List<CatalogueComment> {
        if (catalogue != null && catalogue?.value != null && catalogue?.value?.data != null
                && catalogue?.value?.data?.comments != null) {
            return catalogue?.value?.data?.comments!!

        }
        return Collections.emptyList<CatalogueComment>()
    }

    fun like(catalogueContainer: CatalogueContainer) {
        var request = ""
        if (Functions.isLike(sharedPreferenceHelper, catalogueContainer.likes)) {
            request = "unlike"
        } else {
            request = "like"
        }
        catalogueLike = catalogueRepository.likeCataloguePost(catalogueContainer.cataloguePost.id, RequestLike(request))
    }

    fun comment(content: String) {
        catalogueComment = catalogueRepository.createCommentCataloguePost(postId.value!!, RequestComment(content))
    }

    fun refresh() {
        refresh = true
        forceLoad = true
        page = DEFAULT_PAGE

        this.postId.value = id
    }

    fun loadMore() {
        page++
        forceLoad = true
        refresh = false

        this.postId.value = id
    }

    fun canLoadMore(): Boolean {
        pagination?.nextPage.toString()
        if (pagination != null && pagination?.nextPage != null && pagination?.nextPage != 0
                && pagination?.nextPage.toString() != "") {
            Functions.showLogMessage("Trong", "page current " + page)
            return true
        }
        return false
    }
}