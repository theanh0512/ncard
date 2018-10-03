package com.user.ncard.ui.chats.forward

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import android.util.ArrayMap
import android.util.Log
import android.util.SparseArray
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.ChatDialog
import com.user.ncard.vo.ChatDialogType
import com.user.ncard.vo.Friend
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ForwardListViewModel @Inject constructor(val userRepository: UserRepository,
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
    val groupDialogsSelected: ArrayMap<String, ChatDialog> = ArrayMap()
    var friendsSelected: List<Int>? = ArrayList<Int>()
    var occupants: List<Int>? = ArrayList<Int>()

    lateinit var lsGroupDialogs: LiveData<List<ChatDialog>>
    var chatUserId: Int? = null
    var groupDialogId: String? = null

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
        lsGroupDialogs = chatRepository?.chatDialogDao.findAllGroupDialogs()
    }

    fun initData(chatUserId: Int?, groupDialogId: String?) {
        if (chatUserId != null) {
            this.chatUserId = chatUserId
        }
        if (groupDialogId != null) {
            this.groupDialogId = groupDialogId
        }
    }

    /**
     * Get items list
     */
    fun getItems(): List<Friend> {
        if (items != null && items?.value != null && items?.value?.data != null) {
            if (this.chatUserId != null) {
                return items?.value?.data?.filter { chatUserId != it.chatId }!!
            }
            return items?.value?.data!!
        }
        return Collections.emptyList<Friend>()
    }

    fun getChatDialogGroup(): List<ChatDialog> {
        if (lsGroupDialogs != null && lsGroupDialogs.value != null && lsGroupDialogs.value?.isNotEmpty()!!) {
            if (groupDialogId != null) {
                return lsGroupDialogs?.value?.filter { groupDialogId != it.dialogId }!!
            }
            return lsGroupDialogs.value!!
        }
        return Collections.emptyList<ChatDialog>()
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