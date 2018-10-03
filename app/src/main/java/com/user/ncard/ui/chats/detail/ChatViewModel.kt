package com.user.ncard.ui.chats.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.os.AsyncTask
import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import com.quickblox.chat.QBChatService
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.utils.MongoDBObjectId
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.WalletRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.util.*
import com.user.ncard.vo.*
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.SmackException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * Created by dangui on 13/11/17.
 */
class ChatViewModel @Inject constructor(val chatRepository: ChatRepository,
                                        val walletRepository: WalletRepository,
                                        val sharedPreferenceHelper: SharedPreferenceHelper,
                                        val chatHelper: ChatHelper) : BaseViewModel() {
    private val TAG = "ChatViewModel"
    var dialogId = MutableLiveData<String>()
    lateinit var chatDialog: LiveData<ChatDialog>
    val start = MutableLiveData<Boolean>()
    lateinit var items: LiveData<ResourcePaging<List<ChatMessage>>>
    lateinit var chatMessage: LiveData<Resource<QBChatMessage>>
    lateinit var chatMessageDelete: LiveData<Resource<ChatMessage>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    var qbChatDialog: QBChatDialog = QBChatDialog()
    var activeChatListener: QBChatDialogMessageListener? = null
    var chatConnectionListener: ConnectionListener? = null

    /* Forward message */
    var sparseDialogs: ArrayMap<String, QBChatDialog>   // Store all dialogs private and group
    var allFriends: List<Friend>? = null
    var forwardException = MutableLiveData<Exception?>() // Control exception from forwarding message
    var forwardSuccess = MutableLiveData<QBChatMessage?>() // Control success from forwarding message

    // Credit, gift
    lateinit var transferCreditResponse: LiveData<Resource<TransferCreditResponse>>
    lateinit var sendGiftResponse: LiveData<Resource<SendGiftResponse>>
    lateinit var walletInfo: LiveData<WalletInfo>
    val startWallet = MutableLiveData<Boolean>()
    var friendChat: Friend? = null

    var id: String? = null

    init {
        forceLoad = true
        sparseDialogs = ArrayMap()
        initData()
    }

    override fun initData() {
        chatDialog = Transformations.switchMap(dialogId) { dialogId ->
            if (dialogId == null) {
                return@switchMap AbsentLiveData.create<ChatDialog>()
            }
            return@switchMap chatRepository.getDialogById(dialogId)
        }
        items = Transformations.switchMap(start) { start ->
            if (dialogId == null) {
                return@switchMap AbsentLiveData.create<ResourcePaging<List<ChatMessage>>>()
            } else {
                return@switchMap chatRepository.getMessagesOfDialog(qbChatDialog!!, refresh, forceLoad, page)
            }
        }
        /*chatMessageForward = Transformations.switchMap(startForward) { start ->
            if (start == null || messageForward == null) {
                return@switchMap AbsentLiveData.create<Resource<QBChatMessage>>()
            } else {
                return@switchMap chatRepository.forwardChatMessage(dialogForward!!, messageForward!!)
            }
        }*/

        walletInfo = Transformations.switchMap(startWallet) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<WalletInfo>()
            } else {
                return@switchMap walletRepository.getWalletInfo()
            }
        }
    }

    fun initData(dialogId: String?) {
        this.id = dialogId
        initData()
        this.dialogId.value = this.id

        // Get wallet info
        startWallet.value = true

    }

    fun initQbChatDialog() {
        qbChatDialog = ChatConverter.convertChatDialogToQbChatDialog(chatDialog?.value!!)

        // Get friend chat
        if (chatDialog?.value?.type == ChatDialogType.PRIVATE.type) {
            object : AsyncTask<Void, Void, List<ChatDialog>?>() {
                override fun doInBackground(vararg params: Void): List<ChatDialog>? {
                    // get user chatting
                    var friendChatId: Int? = null
                    chatDialog.value?.occupantIds?.forEach {
                        if (it != Functions.getMyChatId(sharedPreferenceHelper)) {
                            friendChatId = it!!
                        }
                    }
                    if (friendChatId != null) {
                        friendChat = chatRepository?.friendDao.findFriendsByChatId(friendChatId!!)
                    }
                    return null
                }
            }.execute()
        }
    }

    fun updateQbChatDialog() {
        // update name and occupants id
        qbChatDialog.name = chatDialog?.value?.name
        qbChatDialog.setOccupantsIds(chatDialog?.value?.occupantIds)
    }

    fun sendChatMessage(qbChatDialog: QBChatDialog, text: String?, type: String, chat_file: String?, chat_location: String?,
                        credit_transaction: String?, gift: String?, system_info: String?) {
        try {
            //create QBChatMessage
            val qbChatMessage = QBChatMessage()
            text?.let { qbChatMessage.body = text }
            val dateNow = Date()
            val dateSent = dateNow.time / 1000
            qbChatMessage.dateSent = dateSent
            qbChatMessage.isMarkable = true
            qbChatMessage.setSaveToHistory(true)
            qbChatMessage.senderId = Functions.getMyChatId(sharedPreferenceHelper)

            //custom params
            qbChatMessage.setProperty(Constants.CHAT_CONTENT_TYPE, type)
            qbChatMessage.setProperty(Constants.SYSTEM_MESSAGE_TYPE, QMMessageType.normal.type.toString()) // we put QMMessageType here
            qbChatMessage.setProperty(Constants.SAVE_TO_HISTORY, "1")
            qbChatMessage.setProperty(Constants.SENDER_DATE_SENT, dateSent.toString())
            chat_file?.let { qbChatMessage.setProperty(Constants.CHAT_FILE, chat_file) }
            chat_location?.let { qbChatMessage.setProperty(Constants.CHAT_LOCATION, chat_location) }
            credit_transaction?.let { qbChatMessage.setProperty(Constants.CHAT_CREDIT_TRANSACTION, credit_transaction) }
            gift?.let { qbChatMessage.setProperty(Constants.CHAT_GIFT, gift) }
            system_info?.let { qbChatMessage.setProperty(Constants.CHAT_SYSTEM_INFO, system_info) }

            // Send message
            chatMessage = chatRepository.sendChatMessage(qbChatDialog, qbChatMessage)
        } catch (e: SmackException.NotConnectedException) {
            Utils.Log(TAG, e.localizedMessage)
        }
    }

    fun joinGroupChat(callback: QBEntityCallback<Void>) {
        chatHelper.join(qbChatDialog, callback)
    }

    fun deleteMessage(chatMessage: ChatMessage) {
        try {
            // Send message
            chatMessageDelete = chatRepository.deleteChatMessage(qbChatDialog!!, chatMessage)
        } catch (e: SmackException.NotConnectedException) {
            Utils.Log(TAG, e.localizedMessage)
        }
    }

    fun processReceivedMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage?) {
        qbChatMessage?.let {
            chatRepository.insertChatMessageToDb(qbChatDialog, qbChatMessage, false)
        }
    }

    fun processRemovedMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage?) {
        qbChatMessage?.let {
            chatRepository.deleteChatMessageById(qbChatMessage)
        }
    }

    fun setupActivePrivateChatDialog(privateChatListener: QBChatDialogMessageListener) {
        activeChatListener = privateChatListener
        qbChatDialog.initForChat(QBChatService.getInstance())
        qbChatDialog.addMessageListener(privateChatListener)
    }

    fun removeActivePrivateChatListener(qbChatDialog: QBChatDialog) {
        qbChatDialog.removeMessageListrener(activeChatListener)
    }

    fun initChatConnectionListener(chatConnectionListener: ConnectionListener) {
        this.chatConnectionListener = chatConnectionListener
    }

    /*fun markAllMessagesAsRead(QBEntityCallback) {
        QBRestChatService.markMessagesAsRead(qbChatDialog.dialogId, null).performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void, bundle: Bundle) {}

            override fun onError(e: QBResponseException) {}
        })
    }*/

    var listFriends: List<Friend>? = null
    var listGroupDialogs: List<ChatDialog>? = null
    /* Forward message */
    fun forwardMessage(listFriends: List<Friend>?, listGroupDialogs: List<ChatDialog>?, text: String?, type: String, chat_file: String?, chat_location: String?) {
        try {
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

            // Send message
            canForward = true
            prepareChatDialogs(listFriends, listGroupDialogs, qbChatMessage)
        } catch (e: SmackException.NotConnectedException) {
            Utils.Log(TAG, e.localizedMessage)
        }
    }

    fun prepareChatDialogs(listFriends: List<Friend>?, listGroupDialogs: List<ChatDialog>?, qbChatMessage: QBChatMessage) {

        this.listFriends = listFriends
        this.listGroupDialogs = listGroupDialogs
        var currentUser: User? = null
        val totalDialogs: Int = (if (this.listFriends != null && this.listFriends?.isNotEmpty()!!) this.listFriends?.size!! else 0) + (if (this.listGroupDialogs != null && this.listGroupDialogs?.isNotEmpty()!!) this.listGroupDialogs?.size!! else 0)

        run {
            object : AsyncTask<Void, Void, List<ChatDialog>?>() {
                override fun doInBackground(vararg params: Void): List<ChatDialog>? {
                    allFriends = chatRepository?.friendDao.loadAllFriends()     // Set name and avatar for sender id
                    currentUser = chatRepository?.userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    // Get all local dialogs to compare occupants id
                    val listAllPrivateChatDialogs: MutableList<ChatDialog> = chatRepository?.chatDialogDao.findAllPrivateDialogs() as MutableList<ChatDialog>
                    return listAllPrivateChatDialogs
                }

                override fun onPostExecute(listAllPrivateChatDialogs: List<ChatDialog>?) {
                    var allFriendsHasPrivateDialogs = false
                    // Check private dialog has occupants id between memberid and me locally
                    if (listAllPrivateChatDialogs != null && listAllPrivateChatDialogs!!.isNotEmpty() && listFriends != null && listFriends?.size > 0) {
                        listAllPrivateChatDialogs?.forEach {
                            if (it.type == ChatDialogType.PRIVATE.type) {
                                setOccupantIdsInBroadCastGroupMembers(it, it.occupantIds, allFriends, currentUser)!!
                            }
                        }
                        if (sparseDialogs.size == listFriends.size) {
                            allFriendsHasPrivateDialogs = true
                        }
                    }
                    // Add group dialog
                    if (listGroupDialogs != null && listGroupDialogs.isNotEmpty()) {
                        val qbChatDialog = ChatConverter.convertListChatDialogToQbChatDialog(listGroupDialogs)
                        qbChatDialog?.forEach {
                            it.initForChat(QBChatService.getInstance())
                            Functions.showLogMessage("Trong", "Join " + it.isJoined)
                            chatHelper.join(it, object : QBEntityCallback<Void> {
                                override fun onSuccess(result: Void?, b: Bundle?) {
                                    Functions.showLogMessage("Trong", "Joined " + it.isJoined)
                                    sparseDialogs.put(it.dialogId, it)
                                    checkJoined(qbChatMessage, totalDialogs)
                                }

                                override fun onError(error: QBResponseException?) {
                                    Functions.showLogMessage("Trong", "QBResponseException " + error?.message)
                                }
                            })

                        }
                    }
                    // check
                    if (sparseDialogs?.size > 0 && sparseDialogs?.size == totalDialogs) {
                        // Send message now
                        forwardMessage(qbChatMessage, totalDialogs)
                    } else if (!allFriendsHasPrivateDialogs && listFriends != null && listFriends?.isNotEmpty()!!) {
                        // create new private group for user has no private chat
                        val usersNoHavePrivateChat: MutableList<Friend> = ArrayList<Friend>()
                        listFriends?.forEach {
                            val friend = findFriendFromUserId(allFriends, it.id)
                            if (friend != null) {
                                usersNoHavePrivateChat.add(friend)
                            }
                        }
                        if (usersNoHavePrivateChat.size > 0) {
                            createListDialogs(usersNoHavePrivateChat, currentUser!!, qbChatMessage, totalDialogs)
                        }
                    }
                }
            }.execute()
        }
    }

    fun checkJoined(qbChatMessage: QBChatMessage, totalDialogs: Int) {
        if (sparseDialogs?.size > 0 && sparseDialogs?.size == totalDialogs) {
            // Send message now
            forwardMessage(qbChatMessage, totalDialogs)
        }
    }

    var canForward = false
    // TODO: need to process async here, do a trick for fast
    fun forwardMessage(qbChatMessage: QBChatMessage, totalDialogs: Int) {
        if (sparseDialogs?.size > 0 && sparseDialogs?.size == totalDialogs && canForward) {
            // Send message now
            // Send message for multi private chat
            if (sparseDialogs.size > 0) {
                for (i in 0 until sparseDialogs.size) {
                    val key = sparseDialogs.keyAt(i)

                    // Create new chat message here
                    val chatMessage = QBChatMessage()
                    qbChatMessage.body?.let { chatMessage.body = qbChatMessage.body }
                    chatMessage.dialogId = sparseDialogs.get(key)?.dialogId
                    chatMessage.dateSent = Date().time / 1000
                    chatMessage.isMarkable = qbChatMessage.isMarkable
                    chatMessage.setSaveToHistory(true)
                    chatMessage.senderId = qbChatMessage.senderId

                    val properties = qbChatMessage.properties
                    //custom params
                    chatMessage.setProperty(Constants.CHAT_CONTENT_TYPE, properties.get(Constants.CHAT_CONTENT_TYPE))
                    chatMessage.setProperty(Constants.SYSTEM_MESSAGE_TYPE, QMMessageType.normal.type.toString()) // we put QMMessageType here
                    chatMessage.setProperty(Constants.SAVE_TO_HISTORY, "1")
                    chatMessage.setProperty(Constants.SENDER_DATE_SENT, chatMessage.dateSent.toString())
                    properties.get(Constants.CHAT_FILE)?.let { chatMessage.setProperty(Constants.CHAT_FILE, properties.get(Constants.CHAT_FILE)) }
                    properties.get(Constants.CHAT_LOCATION)?.let { chatMessage.setProperty(Constants.CHAT_LOCATION, properties.get(Constants.CHAT_LOCATION)) }

                    Functions.showLogMessage("Trong", "Forward message  " + sparseDialogs.get(key)?.name + " message " + chatMessage.id)
                    chatRepository.forwardChatMessage(sparseDialogs.get(key)!!, chatMessage, object : QBEntityCallback<Void> {
                        override fun onSuccess(void: Void?, bundle: Bundle?) {
                            forwardSuccess.value = chatMessage
                        }

                        override fun onError(error: QBResponseException?) {
                            forwardException.value = error
                        }

                    })
                }
                canForward = false
            }

        }
    }

    fun createListDialogs(usersNoHavePrivateChat: MutableList<Friend>, currentUser: User, qbChatMessage: QBChatMessage, totalDialogs: Int) {
        usersNoHavePrivateChat?.forEach {
            val name = String.format("%s %s", it?.firstName, it?.lastName)
            chatHelper.createChatDialog(arrayListOf(it.chatId!!) as List<Int>, name, object : ChatHelper.CreateChatDialogListener {
                override fun onCreateSuccess(qbChatDialog: QBChatDialog) {
                    sparseDialogs.put(qbChatDialog.dialogId, qbChatDialog)
                    forwardMessage(qbChatMessage, totalDialogs)
                }

                override fun onCreateError() {
                }
            })
        }
    }

    fun setOccupantIdsInBroadCastGroupMembers(chatDialog: ChatDialog, occupantIds: List<Int>?, friends: List<Friend>?, current: User?): Boolean {
        if (occupantIds != null) {
            this.listFriends?.forEach {
                val findChatIdFromUserId = findChatIdFromUserId(friends, it.id)
                if (findChatIdFromUserId != null && occupantIds?.containsAll(arrayListOf(current?.chatId, findChatIdFromUserId))!!) {
                    val qbChatDialog = ChatConverter.convertChatDialogToQbChatDialog(chatDialog)
                    qbChatDialog?.let {
                        qbChatDialog.initForChat(QBChatService.getInstance())
                        sparseDialogs.put(qbChatDialog.dialogId, qbChatDialog)
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
    /* Forward message */

    /*Credit, gift*/
    fun getCreditTransactionDetail(chatMessage: ChatMessage, transactionId: Int) {
        transferCreditResponse = chatRepository.getCreditTransactionDetail(chatMessage, transactionId)
    }

    fun updateCreditTransactionDetail(chatMessage: ChatMessage, transactionId: Int, body: RequestUpdateCreditTransaction) {
        transferCreditResponse = chatRepository.updateCreditTransactionDetail(chatMessage, transactionId, body)
    }

    fun getGiftResponseDetail(chatMessage: ChatMessage, giftId: Int) {
        sendGiftResponse = chatRepository.getGiftDetail(chatMessage, giftId)
    }

    fun updateGiftResponseDetail(chatMessage: ChatMessage, giftId: Int, body: RequestUpdateGift) {
        sendGiftResponse = chatRepository.updateGiftDetail(chatMessage, giftId, body)
    }
    /*Credit, gift*/

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