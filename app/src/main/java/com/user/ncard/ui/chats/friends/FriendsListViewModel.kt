package com.user.ncard.ui.chats.friends

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Log
import android.util.SparseArray
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Friend
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by trong-android-dev on 22/11/17.
 */

class FriendsListViewModel @Inject constructor(val userRepository: UserRepository,
                                               val chatRepository: ChatRepository,
                                               val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {

    val start = MutableLiveData<Boolean>()
    lateinit var items: LiveData<ResourcePaging<List<Friend>>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    val itemsSelected: SparseArray<Friend> = SparseArray()
    var friendsSelected: List<Int>? = ArrayList<Int>()
    var occupants: List<Int>? = ArrayList<Int>()

    init {
        forceLoad = false
        initData()
    }

    override fun initData() {
        items = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<ResourcePaging<List<Friend>>>()
            }
            isLoading = true
            return@switchMap chatRepository.getALlFriends()
        }
    }

    fun init(friendsSelected: List<Int>?,  occupants: List<Int>?) {
        this.friendsSelected = friendsSelected
        this.occupants = occupants
    }

    fun initFriendsSelected() {
        if (friendsSelected!= null && friendsSelected?.isNotEmpty()!! && items?.value?.data != null && items?.value?.data?.isNotEmpty()!!) {
            items?.value?.data?.forEach { item ->
                friendsSelected?.forEach { friend ->
                    if (friend == item.id) {
                        itemsSelected.put(item.id, item)
                    }
                }
            }
        }
    }

    /**
     * Get items list
     */
    fun getItems(): List<Friend> {
        if (items != null && items?.value != null && items?.value?.data != null) {
            return items?.value?.data!!

        }
        return Collections.emptyList<Friend>()
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