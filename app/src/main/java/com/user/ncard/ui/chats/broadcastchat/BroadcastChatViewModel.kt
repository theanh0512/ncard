package com.user.ncard.ui.chats.broadcastchat

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.user.ncard.repository.ChatRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.util.*
import com.user.ncard.vo.*
import org.jivesoftware.smack.SmackException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by dangui on 13/11/17.
 */
class BroadcastChatViewModel @Inject constructor(val chatRepository: ChatRepository,
                                                 val chatHelper: ChatHelper,
                                                 val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {
    private val TAG = "BroadcastChatViewModel"
    val start = MutableLiveData<Boolean>()
    lateinit var items: LiveData<ResourcePaging<List<ChatMessage>>>
    val startLoadUsers = MutableLiveData<Boolean>()
    lateinit var userNotFriends: LiveData<Resource<List<Friend>>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    lateinit var broadcastGroup: BroadcastGroup
    var sparsePrivateDialog: SparseArray<QBChatDialog>   // Store private chat between current user and opponets
    var allFriends: List<Friend>? = null
    var friendIds: String = "" // id of user not friend

    var forwardException = MutableLiveData<Exception?>() // Control exception from broadcasting message
    var forwardSuccess = MutableLiveData<QBChatMessage?>() // Control success from broadcasting message

    init {
        sparsePrivateDialog = SparseArray()
        forceLoad = false
        initData()
    }

    override fun initData() {
        items = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<ResourcePaging<List<ChatMessage>>>()
            } else {
                return@switchMap chatRepository.getBroadcastChatMessages(broadcastGroup)
            }
        }
        // chatMessage = AbsentLiveData.create<Resource<QBChatMessage>>()
        userNotFriends = Transformations.switchMap(startLoadUsers) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<Friend>>>()
            } else {
                return@switchMap chatRepository?.getFriendByIds(friendIds.dropLast(1), null)
            }
        }
    }

    fun initData(broadcastGroup: BroadcastGroup) {
        this.broadcastGroup = broadcastGroup

        getUsersNotFriend()
    }

    fun getUsersNotFriend() {
        val memberIds = broadcastGroup.memberIds

        object : AsyncTask<Void, Void, List<Friend>?>() {
            override fun doInBackground(vararg params: Void): List<Friend>? {
                allFriends = chatRepository?.friendDao.loadAllFriends()     // Set name and avatar for sender id
                if (allFriends != null && allFriends?.isNotEmpty()!! && memberIds != null && memberIds.size > 0) {
                    memberIds.forEach { memberId ->
                        var isMemberInAllFriend = false
                        allFriends?.forEach { friend ->
                            if (friend.id == memberId) {
                                isMemberInAllFriend = true
                            } else {

                            }
                        }
                        if (!isMemberInAllFriend) {
                            friendIds = friendIds.plus(memberId).plus(",")
                        }
                    }
                }

                return allFriends
            }

            override fun onPostExecute(result: List<Friend>?) {
                super.onPostExecute(result)
                if (friendIds.isNotBlank()) {
                    // Have members are not friends
                    startLoadUsers.value = true
                } else {
                    // all members are in friends list
                    prepareListPrivateChatDialogs()
                }
            }
        }.execute()
    }

    fun processUserNotFriends() {
        (allFriends as MutableList).addAll(userNotFriends?.value?.data!!)
    }

    fun prepareListPrivateChatDialogs() {

        val memberIds = broadcastGroup.memberIds
        var currentUser: User? = null

        memberIds?.let {
            object : AsyncTask<Void, Void, List<ChatDialog>?>() {
                override fun doInBackground(vararg params: Void): List<ChatDialog>? {
                    currentUser = chatRepository?.userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    // Get all local dialogs to compare occupants id
                    val listAllPrivateChatDialogs = chatRepository?.chatDialogDao.findAllPrivateDialogs()
                    return listAllPrivateChatDialogs
                }

                override fun onPostExecute(listAllPrivateChatDialogs: List<ChatDialog>?) {
                    // CHeck private dialog has occupants id between memberid and me locally
                    if (listAllPrivateChatDialogs != null && listAllPrivateChatDialogs.isNotEmpty()) {
                        listAllPrivateChatDialogs?.forEach {
                            if (it.type == ChatDialogType.PRIVATE.type) {
                                setOccupantIdsInBroadCastGroupMembers(it, it.occupantIds, allFriends, currentUser)!!
                            }
                        }
                    }
                    // check
                    if (sparsePrivateDialog?.size() > 0 && memberIds.size > 0 && sparsePrivateDialog?.size() == memberIds.size) {
                        // Do nothing
                    } else {
                        // create new private dialog for user has no private chat
                        val usersNoHavePrivateChat: MutableList<Friend> = ArrayList<Friend>()
                        memberIds?.forEach {
                            val friend = findFriendFromUserId(allFriends, it)
                            if (friend != null) {
                                usersNoHavePrivateChat.add(friend)
                            }
                        }
                        if (usersNoHavePrivateChat.size > 0) {
                            createListDialogs(usersNoHavePrivateChat, currentUser!!)
                        }
                    }
                }
            }.execute()
        }
    }

    fun createListDialogs(usersNoHavePrivateChat: MutableList<Friend>, currentUser: User) {
        usersNoHavePrivateChat?.forEach {
            val name = String.format("%s %s", it?.firstName, it?.lastName)
            chatHelper.createChatDialog(arrayListOf(it.chatId!!) as List<Int>, name, object : ChatHelper.CreateChatDialogListener {
                override fun onCreateSuccess(qbChatDialog: QBChatDialog) {
                    sparsePrivateDialog.put(it.chatId!!, qbChatDialog)
                }

                override fun onCreateError() {
                }
            })
        }
    }

    fun setOccupantIdsInBroadCastGroupMembers(chatDialog: ChatDialog, occupantIds: List<Int>?, friends: List<Friend>?, current: User?): Boolean {
        if (occupantIds != null) {
            broadcastGroup.memberIds?.forEach {
                val findChatIdFromUserId = findChatIdFromUserId(friends, it)
                if (findChatIdFromUserId != null && occupantIds?.containsAll(arrayListOf(current?.chatId, findChatIdFromUserId))!!) {
                    val qbChatDialog = ChatConverter.convertChatDialogToQbChatDialog(chatDialog)
                    qbChatDialog?.let {
                        qbChatDialog.initForChat(QBChatService.getInstance())
                        sparsePrivateDialog.put(findChatIdFromUserId!!, qbChatDialog)
                    }
                    return true
                }
            }
        }
        return false
    }

    fun findChatIdFromUserId(friends: List<Friend>?, id: Int): Int? {
        friends?.let {
            friends?.forEach {
                if (it.id == id) {
                    return it.chatId
                }
            }
        }
        return null
    }

    fun findFriendFromUserId(friends: List<Friend>?, id: Int): Friend? {
        friends?.let {
            friends?.forEach {
                if (it.id == id) {
                    return it
                }
            }
        }
        return null
    }

    fun sendChatMessage(broadcastGroup: BroadcastGroup, text: String?, type: String, chat_file: String?, chat_location: String?) {
        try {

            // Send message for multi private chat
            if (sparsePrivateDialog.size() > 0) {

                for (i in 0 until sparsePrivateDialog.size()) {
                    val key = sparsePrivateDialog.keyAt(i)

                    //create QBChatMessage
                    val qbChatMessage = QBChatMessage()
                    text?.let { qbChatMessage.body = text }
                    val dateNow = Date()
                    val dateSent = dateNow.time / 1000
                    qbChatMessage.dateSent = dateSent
                    qbChatMessage.isMarkable = false
                    qbChatMessage.setSaveToHistory(true)
                    qbChatMessage.senderId = Functions.getMyChatId(sharedPreferenceHelper)

                    //custom params
                    qbChatMessage.setProperty(Constants.CHAT_CONTENT_TYPE, type)
                    qbChatMessage.setProperty(Constants.SYSTEM_MESSAGE_TYPE, QMMessageType.normal.type.toString()) // we put QMMessageType here
                    qbChatMessage.setProperty(Constants.SAVE_TO_HISTORY, "1")
                    qbChatMessage.setProperty(Constants.SENDER_DATE_SENT, dateSent.toString())
                    chat_file?.let { qbChatMessage.setProperty(Constants.CHAT_FILE, chat_file) }
                    chat_location?.let { qbChatMessage.setProperty(Constants.CHAT_LOCATION, chat_location) }

                    Functions.showLogMessage("Trong", "send BroadcastChatMessage  " + sparsePrivateDialog.get(key).name)
                    chatRepository.broadcastChatMessage(broadcastGroup, sparsePrivateDialog.get(key)!!, qbChatMessage, object : QBEntityCallback<Void> {
                        override fun onSuccess(void: Void?, bundle: Bundle?) {
                            // Just insert the last message to broadcsast group as a message
                            if (i == sparsePrivateDialog.size() - 1) {
                                chatRepository.insertBroadcastChatMessageToDb(broadcastGroup, sparsePrivateDialog.get(key)!!, qbChatMessage)
                            }
                            forwardSuccess.value = qbChatMessage
                        }

                        override fun onError(error: QBResponseException?) {
                            forwardException.value = error
                        }

                    })
                }
            }
        } catch (e: SmackException.NotConnectedException) {
            Utils.Log(TAG, e.localizedMessage)
        }
    }

    /**
     * Get items list
     */
    fun getItems(): List<ChatMessage> {
        if (items != null && items?.value != null && items?.value?.data != null) {
            return items?.value?.data?.reversed()!!

        }
        return Collections.emptyList<ChatMessage>()
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
            Log.d("ChatViewModel", "page current " + page)
            return true
        }
        return false
    }
}