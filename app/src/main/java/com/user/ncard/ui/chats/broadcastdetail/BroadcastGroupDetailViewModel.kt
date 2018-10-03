package com.user.ncard.ui.chats.broadcastdetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Log
import com.user.ncard.repository.BroadcastGroupRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.MessageObject
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailFragment.Companion.TYPE_VIEW
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Resource
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by trong-android-dev on 22/11/17.
 */

class BroadcastGroupDetailViewModel @Inject constructor(val userRepository: UserRepository,
                                                        val broadcastGroupRepository: BroadcastGroupRepository,
                                                        val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {
    val create = MutableLiveData<Boolean>()
    lateinit var broadcastGroupCreate: LiveData<Resource<BroadcastGroup>>
    var groupId = MutableLiveData<Int>()
    lateinit var item: LiveData<Resource<BroadcastGroup>>
    val delete = MutableLiveData<Boolean>()
    lateinit var deleteItem: LiveData<Resource<MessageObject>>
    val update = MutableLiveData<Boolean>()
    lateinit var updateItem: LiveData<Resource<MessageObject>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    var type: Int = TYPE_VIEW
    var id: Int = -1

    // From select friends
    var listFriends: MutableList<Friend> = ArrayList<Friend>()
    lateinit var broadcastGroup: BroadcastGroup

    init {
        forceLoad = true
        initData()
    }

    override fun initData() {
        // Load
        item = Transformations.switchMap(groupId) { groupId ->
            if (groupId == null || groupId == -1) {
                return@switchMap AbsentLiveData.create<Resource<BroadcastGroup>>()
            }
            isLoading = true
            return@switchMap broadcastGroupRepository.getBroadcastGroupDetail(refresh, forceLoad, groupId)
        }
        // Create
        broadcastGroupCreate = Transformations.switchMap(create) { create ->
            if (create == null) {
                return@switchMap AbsentLiveData.create<Resource<BroadcastGroup>>()
            }

            return@switchMap broadcastGroupRepository.createBroadcastGroup(broadcastGroup)
        }
        // Delete
        deleteItem = Transformations.switchMap(delete) { start ->
            if (start == null || groupId?.value == -1) {
                return@switchMap AbsentLiveData.create<Resource<MessageObject>>()
            }
            return@switchMap broadcastGroupRepository.deleteCataloguePost(groupId.value!!)
        }
        // Update
        updateItem = Transformations.switchMap(update) { start ->
            if (start == null || groupId?.value == -1) {
                return@switchMap AbsentLiveData.create<Resource<MessageObject>>()
            }
            return@switchMap broadcastGroupRepository.updateBroadcastGroup(groupId.value!!, broadcastGroup)
        }
    }

    fun init(type: Int, id: Int) {
        this.type = type
        this.id = id

        initData()
    }

    open fun removeAllMembersBroadcastGroup() {
        if (item != null && item?.value != null && item?.value?.data != null
                && item?.value?.data?.members != null) {
            val members = item?.value?.data?.members as MutableList
            members.clear()
        }
    }

    fun removeMemberAt(position: Int) {
        // Choose friends selected first
        if (listFriends?.isNotEmpty()) {
            listFriends.removeAt(position)
        } else if (item != null && item?.value != null && item?.value?.data != null
                && item?.value?.data?.members != null) {
            val members = item?.value?.data?.members as MutableList
            members.removeAt(position)
        }

    }

    open fun getBroadcastMember(): List<Int>? {
        // Choose friends selected first
        if (listFriends?.isNotEmpty()) {
            return listFriends.map { it.id }
        } else if (item != null && item?.value != null && item?.value?.data != null
                && item?.value?.data?.members != null && item?.value?.data?.members?.isNotEmpty()!!) {
            return item?.value?.data?.members?.map { it.id }!!
        }
        return null
    }

    fun createOrUpadateBroadcastGroup(broadcastGroup: BroadcastGroup) {
        this.broadcastGroup = broadcastGroup
        if (type == BroadcastGroupDetailFragment.TYPE_CREATE) {
            create.value = true
        } else if (type == BroadcastGroupDetailFragment.TYPE_UPDATE) {
            update.value = true
        }
    }

    /**
     * Get items list
     */
    fun getItems(): List<BroadcastGroup.Member> {
        // Choose friends selected first
        if (listFriends?.isNotEmpty()) {
            return ChatConverter.convertListFriendsToBroadcastMember(listFriends)
        }
        if (item != null && item?.value != null && item?.value?.data != null
                && item?.value?.data?.members != null) {
            return item?.value?.data?.members!!

        }
        return Collections.emptyList<BroadcastGroup.Member>()
    }

    /**
     * Refresh list
     */
    fun refresh() {
        refresh = true
        forceLoad = true
        page = DEFAULT_PAGE

        this.groupId.value = id
    }

    /**
     * Load more list
     */
    fun loadMore() {
        page++
        forceLoad = true
        refresh = false

        this.groupId.value = id
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