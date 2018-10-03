package com.user.ncard.ui.chats.groups

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import android.util.Log
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.user.ncard.repository.BroadcastGroupRepository
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.MessageObject
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailFragment
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import java.util.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ChatGroupDetailViewModel @Inject constructor(val userRepository: UserRepository,
                                                   val broadcastGroupRepository: BroadcastGroupRepository,
                                                   val chatRepository: ChatRepository,
                                                   val chatHelper: ChatHelper,
                                                   val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {
    val startLoadUsers = MutableLiveData<Boolean>()
    var dialogId = MutableLiveData<String>()
    lateinit var chatDialog: LiveData<ChatDialog>
    lateinit var userNotFriends: LiveData<Resource<List<Friend>>>
    val exit = MutableLiveData<Boolean>()
    lateinit var exitItem: LiveData<Resource<QBChatDialog>>
    val update = MutableLiveData<Boolean>()
    lateinit var updateItem: LiveData<Resource<QBChatDialog>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    var type: Int = BroadcastGroupDetailFragment.TYPE_VIEW

    // From select friends
    var listFriends: MutableList<Friend> = ArrayList<Friend>()
    var qbChatDialog: QBChatDialog = QBChatDialog()
    var allFriends: List<Friend>? = null
    var allFriendsInLocalDb: List<Friend>? = null
    var listUserNotFriends: MutableList<Friend> = ArrayList<Friend>()
    var friendChatIds: String = "" // id of user not friend

    var id: String? = null

    init {
        forceLoad = true
        initData()
    }

    override fun initData() {
        chatDialog = Transformations.switchMap(dialogId) { dialogId ->
            if (dialogId == null) {
                return@switchMap AbsentLiveData.create<ChatDialog>()
            }
            return@switchMap chatRepository.getDialogById(dialogId)
        }
        // Load users not friend
        userNotFriends = Transformations.switchMap(startLoadUsers) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<Friend>>>()
            } else {
                return@switchMap chatRepository?.getFriendsByChatIds(friendChatIds.dropLast(1), null)
            }
        }
        // Delete
        exitItem = Transformations.switchMap(exit) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<QBChatDialog>>()
            }
            return@switchMap chatRepository?.exitFromDialog(qbChatDialog)
        }
        // Update
        updateItem = Transformations.switchMap(update) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<QBChatDialog>>()
            }
            return@switchMap chatRepository?.updateDialogUsers(qbChatDialog.name, qbChatDialog, listFriends)
        }
    }

    fun getUserNotFriend() {
        if (this.qbChatDialog.type.code == ChatDialogType.GROUP.type) {
            listUserNotFriends?.clear()
            listFriends?.clear()
            getUsersNotFriend()
        }
    }

    fun initData(dialogId: String?) {
        this.id = dialogId
        initData()
        this.dialogId.value = this.id
    }

    fun initQbChatDialog() {
        qbChatDialog = ChatConverter.convertChatDialogToQbChatDialog(chatDialog?.value!!)
        qbChatDialog?.initForChat(QBChatService.getInstance())
    }

    fun getUsersNotFriend() {
        val memberIds = qbChatDialog.occupants

        object : AsyncTask<Void, Void, List<Friend>?>() {
            override fun doInBackground(vararg params: Void): List<Friend>? {
                allFriendsInLocalDb = chatRepository?.friendDao.loadAllFriends()
                allFriends = chatRepository?.friendDao.loadAllFriends()     // Set name and avatar for sender id
                if (allFriends != null && allFriends?.isNotEmpty()!! && memberIds != null && memberIds.size > 0) {
                    memberIds.forEach { memberId ->
                        var isMemberInAllFriend = false
                        allFriends?.forEach { friend ->
                            if (friend.chatId == memberId) {
                                isMemberInAllFriend = true
                            } else {

                            }
                        }
                        if (!isMemberInAllFriend) {
                            friendChatIds = friendChatIds.plus(memberId).plus(",")
                        }
                    }
                }

                return allFriends
            }

            override fun onPostExecute(result: List<Friend>?) {
                super.onPostExecute(result)
                if (friendChatIds.isNotBlank()) {
                    // Have members are not friends
                    startLoadUsers.value = true
                } else {
                    // all members are in friends list
                    prepareListFriends()
                }
            }
        }.execute()
    }

    fun processUserNotFriends() {
        listUserNotFriends?.addAll(userNotFriends?.value?.data!!)
        (allFriends as MutableList).addAll(userNotFriends?.value?.data!!)
    }

    fun prepareListFriends() {
        qbChatDialog.occupants?.forEach {
            val friend = findFriendFromChatId(allFriends, it)
            friend?.let {
                if (it.chatId == chatHelper.getCurrentQBUser().id) {
                    // Move current user login to top
                    listFriends.add(0, friend)
                } else {
                    listFriends.add(friend)
                }

            }
        }
    }

    fun findFriendFromChatId(friends: List<Friend>?, id: Int): Friend? {
        friends?.let {
            friends?.forEach {
                if (it.chatId == id) {
                    return it
                }
            }
        }
        return null
    }

    fun removeMemberAt(position: Int) {
        // Choose friends selected first
        if (listFriends?.isNotEmpty()) {
            listFriends.removeAt(position)
        }
    }

    open fun getGroupOccupantIdsSelected(): List<Int>? {
        // Choose friends selected first
        if (listFriends?.isNotEmpty()) {
            return listFriends.map { it.id }
        }
        return null
    }

    open fun updateGroupDialog(name: String, photo: String?) {
        qbChatDialog?.name = name
        if(photo != null) {
            qbChatDialog?.photo = photo
        }
        update.value = true

    }

    open fun exitGroupDialog() {
        exit.value = true
    }

    /**
     * Get items list
     */
    fun getItems(): List<Friend> {
        if (listFriends?.isNotEmpty()) {
            return listFriends

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

        //this..value = id
    }

    /**
     * Load more list
     */
    fun loadMore() {
        page++
        forceLoad = true
        refresh = false

        //this..value = id
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