package com.user.ncard.ui.catalogue.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.util.Log
import com.user.ncard.repository.CatalogueRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.RequestLike
import com.user.ncard.ui.catalogue.ScreenTracking
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by trong-android-dev on 23/10/17.
 */
class CatalogueMainViewModel @Inject constructor(val catalogueRepository: CatalogueRepository,
                                                 val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {
    lateinit var catalogue: LiveData<Resource<CataloguePost>>
    val start = MutableLiveData<Boolean>()
    lateinit var catalogues: LiveData<ResourcePaging<List<CatalogueContainer>>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    val filter = CatalogueFilterEvent()
    var share = ObservableField<String>()
    var tags = ObservableField<String>()
    var category = ObservableField<String>()

    init {
        forceLoad = true
        catalogueRepository.appExecutors.diskIO().execute {
            catalogueRepository.catalogueDao.insertCatalogueScreenTracking(CatalogueScreenTracking(ScreenTracking.ALL, "all"))
        }
        /*initFilter(CatalogueFilterEvent("Public", ArrayList<String>(), null, null, null))
        initData()*/
    }

    override fun initData() {
        catalogues = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<ResourcePaging<List<CatalogueContainer>>>()
            }
            isLoading = true
            return@switchMap catalogueRepository.getCataloguePosts(refresh, forceLoad, filter.toStringfiter(),
                    if (filter.visibility.isNullOrEmpty()) "" else filter?.visibility!!,
                    if (filter.postCategory.isNullOrEmpty()) "" else filter?.postCategory!!,
                    if (filter.keyword.isNullOrEmpty()) "" else filter?.keyword!!, page)
        }
    }

    fun initData(category: String) {
        initFilter(CatalogueFilterEvent(SharePost.SharePostType.ALL.type, ArrayList<String>(), category, null, null))
        initData()
    }

    fun getItems(): List<CatalogueContainer> {
        if (catalogues != null && catalogues?.value != null && catalogues?.value?.data != null) {
            return catalogues?.value?.data!!

        }
        return Collections.emptyList<CatalogueContainer>()
    }

    fun initFilter(filter: CatalogueFilterEvent) {
        val oldFilter = CatalogueFilterEvent(this.filter?.visibility,
                this.filter?.tags, this.filter.postCategory, this.filter.keyword, this.filter?.year)

        if (filter.visibility != null) {
            this.filter.visibility = filter.visibility
        }
        if (filter.tags != null) {
            this.filter.tags = filter.tags
        }
        if (filter.year != null) {
            this.filter.year = filter.year
        }
        if (filter.postCategory != null) {
            this.filter.postCategory = filter.postCategory
        }
        if (filter.keyword != null) {
            this.filter.keyword = filter.keyword
        }

        if (filter.tags != null && filter?.tags?.isNotEmpty()!!) {
            tags.set("Tags (" + this.filter?.tags?.size + ")")
        } else if (filter.tags != null && filter?.tags?.isEmpty()!!) {
            tags.set("Tags")
        }

        if (filter.visibility != null) {
            share.set(this.filter.visibility)
        }
        if (filter.postCategory != null) {
            category.set(this.filter.postCategory)
        }

        // Check filter is new or not
        if (oldFilter?.visibility == this.filter?.visibility && compareLists(oldFilter.tags!!, this.filter?.tags!!) == 0
                && oldFilter?.postCategory == this.filter?.postCategory && oldFilter?.keyword == this.filter?.keyword) {
            // Do nothing
            //Log.d("Trong", "the same")
        } else {
            // Filter data
            //Log.d("Trong", "filter data")
            refresh()
        }
    }

    fun like(catalogueContainer: CatalogueContainer) {
        var request = ""
        if (Functions.isLike(sharedPreferenceHelper, catalogueContainer.likes)) {
            request = "unlike"
        } else {
            request = "like"
        }
        catalogue = catalogueRepository.likeCataloguePost(catalogueContainer.cataloguePost.id, RequestLike(request))
    }

    fun refresh() {
        refresh = true
        forceLoad = true
        page = DEFAULT_PAGE

        start.value = true
    }

    fun loadMore() {
        page++
        forceLoad = true
        refresh = false

        start.value = true
    }

    fun canLoadMore(): Boolean {
        pagination?.nextPage.toString()
        if (pagination != null && pagination?.nextPage != null && pagination?.nextPage != 0
                && pagination?.nextPage.toString() != "") {
            Log.d("Trong", "page current " + page)
            return true
        }
        return false
    }

    fun compareLists(list1: List<Comparable<*>>, list2: List<Comparable<*>>): Int {
        for (i in 0..Math.min(list1.size, list2.size) - 1) {
            val elem1 = list1[i]
            val elem2 = list2[i]

            /*if (elem1.javaClass != elem2.javaClass) {
                TODO("Decide what to do when you encounter values of different classes")
            }*/

            compareValues(elem1, elem2).let {
                if (it != 0) return it
            }
        }
        return compareValues(list1.size, list2.size)
    }


}