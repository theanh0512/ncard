package com.user.ncard.ui.catalogue.my

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.util.Log
import com.user.ncard.repository.CatalogueRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.*
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import java.util.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 23/10/17.
 */
class CatalogueMeViewModel @Inject constructor(val catalogueRepository: CatalogueRepository,
                                               val userRepository: UserRepository,
                                               val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {

    lateinit var catalogue: LiveData<Resource<CataloguePost>>
    val username: MutableLiveData<String>? = MutableLiveData()
    lateinit var user: LiveData<Resource<User>>
    val start = MutableLiveData<Boolean>()
    lateinit var catalogues: LiveData<ResourcePaging<List<CatalogueContainer>>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    var name = ObservableField<String>()
    var avatarUrl = ObservableField<String>()
    var coverUrl = ObservableField<String>()

    var category: String = ""

    init {
        catalogueRepository.appExecutors.diskIO().execute {
            catalogueRepository.catalogueDao.insertCatalogueScreenTracking(
                    CatalogueScreenTracking(ScreenTracking.ME, "me"))
        }
        //initData()
    }

    override fun initData() {
        catalogues = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<ResourcePaging<List<CatalogueContainer>>>()
            }
            isLoading = true
            return@switchMap catalogueRepository.getMyCataloguePosts(refresh, forceLoad, category, page)
        }

        user = Transformations.switchMap(username) { username ->
            if (username == null) {
                return@switchMap AbsentLiveData.create<Resource<User>>()
            } else {
                return@switchMap userRepository.getUserByUserName(username)
            }
        }
    }

    fun initData(category: String?) {
        this.category = category!!
        initData()
    }

    fun setUsername(username: String) {
        if (Objects.equals(this.username?.value, username)) {
            return
        }
        this.username?.value = username
    }

    fun getItems(): List<CatalogueContainer> {
        if (catalogues != null && catalogues?.value != null && catalogues?.value?.data != null) {
            return catalogues?.value?.data!!

        }
        return Collections.emptyList<CatalogueContainer>()
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

}