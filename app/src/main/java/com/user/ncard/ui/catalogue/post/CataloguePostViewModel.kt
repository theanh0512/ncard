package com.user.ncard.ui.catalogue.post

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import com.user.ncard.repository.CatalogueRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.RequestCatalogue
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.vo.CataloguePost
import com.user.ncard.vo.Resource
import javax.inject.Inject


/**
 * Created by trong-android-dev on 20/10/17.
 */
class CataloguePostViewModel @Inject constructor(val catalogueRepository: CatalogueRepository) : BaseViewModel() {

    val createCall = MutableLiveData<Boolean>()
    lateinit var catalogue: LiveData<Resource<CataloguePost>>
    var request = RequestCatalogue()

    var share = ObservableField<String>()
    var tags = ObservableField<String>()
    var category = ObservableField<String>()

    init {
        // initFilterValues("Public", ArrayList(), "")
        initData()
    }

    override fun initData() {
        catalogue = Transformations.switchMap(createCall) { createCall ->
            if (createCall == null) {
                return@switchMap AbsentLiveData.create<Resource<CataloguePost>>()
            }
            return@switchMap catalogueRepository.createCataloguePost(request)
        }
    }

    fun initData(category: String?, share: String?) {
        initFilterValues(share, ArrayList(), category)
    }

    fun createCataloguePost(text: String, photoUrls: List<String>?,
                            videoUrl: String?, videoThumbnailUrl: String?) {
        request.text = text
        request.photoUrls = photoUrls
        request.videoUrl = videoUrl
        request.videoThumbnailUrl = videoThumbnailUrl

        createCall.value = true
    }

    fun initFilterValues(share: String?, tags: List<String>?, category: String?) {
        if (tags != null) {
            request.tags = tags
        }
        if (share != null) {
            request.visibility = share
        }
        if (category != null) {
            request.postCategory = category
        }

        this.share.set(request.visibility)
        if (tags != null && tags?.isNotEmpty()) {
            this.tags.set(request.tags?.size.toString().plus(" Tag(s)"))
        } else if (tags != null && tags?.isEmpty()) {
            this.tags.set("")
        }
        if (request.postCategory != null) {
            this.category.set(request.postCategory)
        }
    }


}