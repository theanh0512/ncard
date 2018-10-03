package com.user.ncard.ui.chats.broadcast

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Log
import android.util.SparseArray
import com.user.ncard.repository.BroadcastGroupRepository
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.Friend
import java.util.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class BroadcastGroupListViewModel @Inject constructor(val broadcastGroupRepository: BroadcastGroupRepository,
                                                      val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {

    val start = MutableLiveData<Boolean>()
    lateinit var items: LiveData<ResourcePaging<List<BroadcastGroup>>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    init {
        forceLoad = true
        initData()
    }

    override fun initData() {
        items = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<ResourcePaging<List<BroadcastGroup>>>()
            }
            isLoading = true
            return@switchMap broadcastGroupRepository.getAllBroadcastGroups(refresh, forceLoad, page)
        }
    }

    /**
     * Get items list
     */
    fun getItems(): List<BroadcastGroup> {
        if (items != null && items?.value != null && items?.value?.data != null) {
            return items?.value?.data!!

        }
        return Collections.emptyList<BroadcastGroup>()
    }

    /**
     * Refresh list
     */
    fun refresh() {
        refresh = true
        forceLoad = true
        page = DEFAULT_PAGE

        start.value = true
    }

    /**
     * Load more list
     */
    fun loadMore() {
        page++
        forceLoad = true
        refresh = false

        start.value = true
    }

    /**
     * Check list can load more or not
     */
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