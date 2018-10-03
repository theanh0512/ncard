package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.user.ncard.AppExecutors
import com.user.ncard.api.ApiResponse
import com.user.ncard.api.ChatService
import com.user.ncard.api.NCardService
import com.user.ncard.db.*
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.ui.catalogue.utils.*
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by dangui on 14/11/17.
 */
@Singleton
class ChatRepository @Inject constructor(val userDao: UserDao,
                                         val chatDialogDao: ChatDialogDao,
                                         val chatMessageDao: ChatMessageDao,
                                         val friendDao: FriendDao,
                                         val db: NcardDb,
                                         val context: Context,
                                         val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                         val nCardService: NCardService,
                                         val chatService: ChatService,
                                         val appExecutors: AppExecutors,
                                         val sharedPreferenceHelper: SharedPreferenceHelper) {
    val MSG_LIMIT = 100

    fun getMessagesOfDialog(dialog: QBChatDialog,
                            refresh: Boolean,
                            forLoad: Boolean,
                            page: Int): LiveData<ResourcePaging<List<ChatMessage>>> {
        return object : NetworkBoundResourcePaging<List<ChatMessage>, ArrayList<QBChatMessage>>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: ArrayList<QBChatMessage>) {
                db.beginTransaction()
                try {
                    if (refresh) {
                        // Remove all messages from dialogId in db
                        chatMessageDao.deleteMessagesFrom(dialog.dialogId)
                    }
                    // Insert messages to db
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    var items = ChatConverter.convertListQbChatMessageToChatMessage(dialog.dialogId, item, allFriends, currentUser)

                    // Get all system messages
                    val itemsSystem = items.filter { it?.customParam.uxc_system_info != null }
                    // Remove system messages here
                    items = items.filter { it?.customParam.uxc_system_info == null }
                    // Process system messages
                    processSystemMessages(items, itemsSystem)

                    (dialog != null && items != null).let {
                        if (items.size > 0 && dialog.unreadMessageCount != null && dialog.unreadMessageCount > 0 && items.size >= dialog.unreadMessageCount) {
                            // Insert all unread messages
                            val itemFilter = items.filterIndexed { index, chatMessage -> index < dialog.unreadMessageCount }
                            if (itemFilter != null) {
                                chatMessageDao.insertChatMessages(itemFilter)
                            }
                        } else {
                            // Insert 1 message to group chat
                            if (dialog.type.code == ChatDialogType.GROUP.type && items.size > 0) {
                                chatMessageDao.insert(items.get(0))
                            }
                            // Sync local and chat quickblox
                            var messagesLocal = chatMessageDao.findChatMessages(dialog.dialogId)
                            val idsMessageLocal = messagesLocal.map { it.messageId }
                            val idsMessageQb = items?.map { it.messageId }
                            if (idsMessageLocal?.isNotEmpty() && idsMessageQb?.isNotEmpty() && messagesLocal?.isNotEmpty()) {
                                // Remove message sent fail
                                if (!idsMessageQb.containsAll(idsMessageLocal)) {
                                    // Find message sent fail
                                    val idsMessageSentFail: MutableList<String> = ArrayList()
                                    idsMessageLocal?.forEach {
                                        if (!idsMessageQb?.contains(it)) {
                                            idsMessageSentFail.add(it!!)
                                        }
                                    }
                                    if (idsMessageSentFail?.isNotEmpty()) {
                                        chatMessageDao.deleteChatMessages(idsMessageSentFail.toTypedArray())
                                    }
                                    // Get local messages again
                                    messagesLocal = chatMessageDao.findChatMessages(dialog.dialogId)
                                }
                                // Sync messages again
                                var itemFilter = items.filterIndexed { index, chatMessage -> index < messagesLocal.size }
                                // Don't sync credit/gift message
                                itemFilter = itemFilter.filter { it.customParam.credit_transaction == null && it.customParam.gift == null }
                                if (itemFilter != null) {
                                    chatMessageDao.insertChatMessages(itemFilter)
                                }
                            }
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun returnPaging(item: ArrayList<QBChatMessage>): Pagination {
                return Pagination()
            }


            override fun shouldFetch(data: List<ChatMessage>?): Boolean {
                return data == null || forLoad || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<ChatMessage>> {
                return return chatMessageDao.findChatMessagesOfDialog(dialog.dialogId, MSG_LIMIT)
            }

            override fun createCall(): LiveData<ApiResponse<ArrayList<QBChatMessage>>> {
                return chatService.getChatMessageFromDialog(dialog)
            }
        }.asLiveData()
    }

    fun processSystemMessages(items: List<ChatMessage>, itemsSystem: List<ChatMessage>) {
        // Update credit transaction message
        if (itemsSystem?.isNotEmpty() && items.isNotEmpty()) {
            // Update credit tranfer
            itemsSystem.forEach { itemsSystem ->
                var chat_credit_transaction: TransferCreditResponse? = null
                var gift: SendGiftResponse? = null
                itemsSystem.customParam.uxc_system_info?.let {
                    val jsonFile: JsonObject = JsonParser().parse(it?.data).asJsonObject
                    jsonFile?.let {
                        if (itemsSystem.customParam.uxc_system_info?.category == Constants.CATEGORY_WALLET) {
                            chat_credit_transaction = Gson().fromJson(
                                    jsonFile,
                                    object : TypeToken<TransferCreditResponse>() {}.type
                            )
                        } else if (itemsSystem.customParam.uxc_system_info?.category == Constants.CATEGORY_ECOMMERCE) {
                            gift = Gson().fromJson(
                                    jsonFile,
                                    object : TypeToken<SendGiftResponse>() {}.type
                            )
                        }
                    }
                }
                run loop@ {
                    if (chat_credit_transaction != null) {
                        items.forEach {
                            if (it.customParam.credit_transaction != null && chat_credit_transaction?.id == it.customParam.credit_transaction?.id) {
                                it.customParam.credit_transaction?.status = chat_credit_transaction?.status!!
                                return@loop
                            }
                        }
                    } else if (gift != null) {
                        items.forEach {
                            if (it.customParam.gift != null && gift?.id == it.customParam.gift?.id) {
                                it.customParam.gift?.status = gift?.status!!
                                return@loop
                            }
                        }
                    }
                }
            }
        }
    }

    fun getBroadcastChatMessages(broadcastGroup: BroadcastGroup): LiveData<ResourcePaging<List<ChatMessage>>> {
        return object : NetworkBoundResourcePaging<List<ChatMessage>, ArrayList<QBChatMessage>>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: ArrayList<QBChatMessage>) {
                db.beginTransaction()
                try {
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun returnPaging(item: ArrayList<QBChatMessage>): Pagination {
                return Pagination()
            }


            override fun shouldFetch(data: List<ChatMessage>?): Boolean {
                return false
            }

            override fun loadFromDb(): LiveData<List<ChatMessage>> {
                val dialogId = "broadcast_" + broadcastGroup.id
                return return chatMessageDao.findChatMessagesOfDialog(dialogId, MSG_LIMIT)
            }

            override fun createCall(): LiveData<ApiResponse<ArrayList<QBChatMessage>>> {
                // Fake call(never call)
                return chatService.getBroadcastChatMessage(broadcastGroup)
            }
        }.asLiveData()
    }

    /*fun createOutgoingTextMessage(qbChatMessage: QBChatMessage, qbChatDialog: QBChatDialog,
                                  myFullName: String, myChatId: Int, dateNow: Date): ChatMessage {
        val messageId = qbChatMessage.id
        val dialogId = qbChatDialog.dialogId
        val recipientId = qbChatDialog.recipientId
        val senderId = myChatId
        val senderName = myFullName

        return ChatMessage(messageId, dialogId, recipientId, senderId, senderName, null, qbChatMessage.body,
                ChatMessageType.NORMAL.type, ChatSystemMessageType.UNKNOWN.type,
                dateNow, dateNow, dateNow, true,
                ChatMessage.ChatMessageCustomParam(ChatMessageContentType.TEXT.type, null,
                        null, null, ChatSystemMessageType.UNKNOWN.type,
                        dateNow))
    }*/

    /*fun createIncomingTextMessage(dialogId: String?, qbChatMessage: QBChatMessage?): ChatMessage? {
        if (dialogId == null || qbChatMessage == null) {
            return null
        }

        val messageId = qbChatMessage.id
        val recipientId = qbChatMessage.recipientId
        val senderId = qbChatMessage.senderId
        //TODO: sender name not in qbChatMessage, get the full name by senderId from friend repository or group chat participant repository
        val senderName = "null"
        //custom properties
        val properties = qbChatMessage?.properties
        val chat_content_type = properties?.get(Constants.CHAT_CONTENT_TYPE)
        val chat_location = properties?.get(Constants.CHAT_LOCATION)
        val chat_file = properties?.get(Constants.CHAT_FILE)
        val system_message_type = properties?.get(Constants.SYSTEM_MESSAGE_TYPE)
        val save_to_history = properties?.get(Constants.SAVE_TO_HISTORY)
        val sender_date_sent = properties?.get(Constants.SENDER_DATE_SENT)

        val dateSentTimestamp = sender_date_sent?.toDouble()?.times(1000)?.toLong()
        val dateSent = dateSentTimestamp?.let { Date(it) }

        return ChatMessage(messageId, dialogId, recipientId, senderId, senderName, null, qbChatMessage.body,
                ChatMessageType.NORMAL.type,
                ChatSystemMessageType.UNKNOWN.type,
                dateSent, dateSent, dateSent, false,
                ChatMessage.ChatMessageCustomParam(chat_content_type!!, null,
                        null, system_message_type, save_to_history,
                        dateSent))
    }*/

    fun getChatDialogs(requestBuilder: QBRequestGetBuilder,
                       refresh: Boolean,
                       forLoad: Boolean,
                       page: Int): LiveData<ResourcePaging<List<ChatDialog>>> {
        return object : NetworkBoundResourcePaging<List<ChatDialog>, ArrayList<QBChatDialog>>(appExecutors, sharedPreferenceHelper) {

            override fun saveCallResult(item: ArrayList<QBChatDialog>) {
                db.beginTransaction()
                try {
                    // Insert dialogs to db
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    val items = ChatConverter.convertListQbChatDialogToChatDialog(item, allFriends, currentUser)
                    // Insert users from dialogs to db
                    val loaded = sharedPreferenceHelper.getBoolean(SharedPreferenceHelper.Key.FIRST_TIME_LOAD_DIALOG_LIST, false)
                    if (!loaded) {
                        if (refresh) {
                            // Remove all in db
                            chatDialogDao.deleteAll()
                        }
                        // add group dialogs and private dialogs with unread messages > 0
                        val itemFilters = items?.filter {
                            it.type == ChatDialogType.GROUP.type
                                    || (it.type == ChatDialogType.PRIVATE.type && it.unreadMessagesCount!! > 0)
                        }
                        chatDialogDao.insertChatDialogs(itemFilters)
                        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.FIRST_TIME_LOAD_DIALOG_LIST, true)
                    } else {
                        // Update private/group dialogs with unread messages > 0 and all groups
                        val itemFilters = items?.filter {
                            // it.type == ChatDialogType.GROUP.type || (
                            it.unreadMessagesCount!! > 0
                            // )
                        }
                        chatDialogDao.insertChatDialogs(itemFilters)
                        // Remove group chat which user is kicked/remove(not available)
                        val allChatDialogsId: List<String> = chatDialogDao.findAllChatDialogsId()
                        val availableChatDialogsId: List<String> = items.map { it.dialogId!! }
                        val unavailableChatDialogsId = allChatDialogsId.filter { !availableChatDialogsId.contains(it) }
                        if (unavailableChatDialogsId != null && unavailableChatDialogsId.isNotEmpty()) {
                            chatDialogDao.deleteChatDialogByIds(unavailableChatDialogsId.toTypedArray())
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun returnPaging(item: ArrayList<QBChatDialog>): Pagination {
                return Pagination()
            }


            override fun shouldFetch(data: List<ChatDialog>?): Boolean {
                return data == null || forLoad || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<ChatDialog>> {
                return chatDialogDao.findAllChatDialogs()
            }

            override fun createCall(): LiveData<ApiResponse<ArrayList<QBChatDialog>>> {
                return chatService.getChatDialogs(requestBuilder)
            }
        }.asLiveData()
    }

    fun getALlFriends(): LiveData<ResourcePaging<List<Friend>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResourcePaging<List<Friend>, List<Friend>>(appExecutors, sharedPreferenceHelper) {


            override fun saveCallResult(item: List<Friend>) {
                db.beginTransaction()
                try {
                    // Do nothing
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun returnPaging(response: List<Friend>?): Pagination? {
                return null
            }

            override fun shouldFetch(data: List<Friend>?): Boolean {
                return false
            }

            override fun loadFromDb(): LiveData<List<Friend>> {
                return friendDao.findAllFriends()
            }

            override fun createCall(): LiveData<ApiResponse<List<Friend>>> {
                return nCardService.getAllFriends(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
            }
        }.asLiveData()
    }

    fun insertDialogToDb(qbChatDialog: QBChatDialog) {
        appExecutors
                .diskIO()
                .execute {
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    val lastMessage = chatMessageDao.findLastChatMessagesOfDialog(qbChatDialog.dialogId)
                    db.beginTransaction()
                    try {
                        val chatDialog = ChatConverter.convertQbChatDialogToChatDialog(qbChatDialog, ChatConverter.findOpponent(qbChatDialog, qbChatDialog.occupants, allFriends, currentUser))
                        if (lastMessage != null && lastMessage.isNotEmpty()) {
                            chatDialog.lastMessageContentType = lastMessage[0]?.customParam?.chat_content_type
                        }
                        chatDialogDao.insert(chatDialog)
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }

                }
    }

    fun deleteDialog(dialogId: String) {
        appExecutors
                .diskIO()
                .execute {
                    chatDialogDao.deleteChatDialogById(dialogId)

                }
    }

    fun insertDialogToDb(chatDialog: ChatDialog) {
        appExecutors
                .diskIO()
                .execute {
                    db.beginTransaction()
                    try {
                        chatDialogDao.insert(chatDialog)
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
    }

    fun sendChatMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage): LiveData<Resource<QBChatMessage>> {
        return object : NetworkCallResource<QBChatMessage>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: QBChatMessage) {
                db.beginTransaction()

                try {
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    chatMessageDao.insert(ChatConverter.convertQbChatMessageToChatMessage(qbChatDialog.dialogId, item, null, currentUser))
                    Functions.showLogMessage("Trong", "save message  " + qbChatMessage.toString())
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<QBChatMessage>> {
                return chatService.sendChatMessage(qbChatDialog, qbChatMessage)
            }
        }.asLiveData()
    }

    fun sendAndSaveChatMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage, callback: QBEntityCallback<Void>) {
        chatService.sendChatMessage(qbChatDialog, qbChatMessage, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                insertChatMessageToDb(qbChatDialog, qbChatMessage, true)
                callback.onSuccess(void, bundle)
            }

            override fun onError(e: QBResponseException) {
                e.printStackTrace()
                callback.onError(e)
            }
        })
    }

    /*fun sendBroadcastChatMessage(broadcastGroup: BroadcastGroup, qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage): LiveData<Resource<QBChatMessage>> {
        return object : NetworkCallResource<QBChatMessage>(appExecutors) {

            override fun returnCallResult(item: QBChatMessage) {
                db.beginTransaction()

                try {
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    val dialogId = "broadcast_" + broadcastGroup.id
                    // item.dialogId = dialogId
                    // Insert message to broadcast group
                    chatMessageDao.insert(ChatConverter.convertQbChatMessageToChatMessage(dialogId, item, null, currentUser))

                    // Insert this message to normal dialog
                    insertChatMessageToDb(qbChatDialog, item)
                    // Find available dialog
                    var chatDialog = chatDialogDao?.findDialogWithId(qbChatDialog.dialogId)
                    if (chatDialog == null) {
                        chatDialogDao.insert(ChatConverter.convertQbChatDialogToChatDialog(qbChatDialog, ChatConverter.findOpponent(qbChatDialog, qbChatDialog.occupants, allFriends, currentUser)))
                    }
                    chatDialog = chatDialogDao?.findDialogWithId(qbChatDialog.dialogId)
                    // Save last message to dialog
                    item?.let {
                        chatDialog.lastMessageText = item.body
                        chatDialog.lastMessageDate = item.dateSent?.times(1000)
                        chatDialog.unreadMessagesCount = (if (chatDialog.unreadMessagesCount != null)
                            chatDialog.unreadMessagesCount!! + 1 else 1)
                        chatDialog.lastMessageSenderId = item.senderId
                        // custom properties
                        val properties = item?.properties
                        chatDialog.lastMessageContentType = properties?.get(Constants.CHAT_CONTENT_TYPE)
                        insertDialogToDb(chatDialog)
                        Functions.showLogMessage("Trong", "insert dialog  " + chatDialog.name)
                    }

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<QBChatMessage>> {
                return chatService.sendChatMessage(qbChatDialog, qbChatMessage)
            }
        }.asLiveData()
    }*/

    fun forwardChatMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage, callback: QBEntityCallback<Void>) {
        sendAndSaveChatMessage(qbChatDialog, qbChatMessage, callback)
    }

    fun broadcastChatMessage(broadcastGroup: BroadcastGroup, qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage, callback: QBEntityCallback<Void>) {
        chatService.sendChatMessage(qbChatDialog, qbChatMessage, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                callback.onSuccess(void, bundle)
                insertChatMessageToDb(qbChatDialog, qbChatMessage, true)
            }

            override fun onError(e: QBResponseException) {
                e.printStackTrace()
                callback.onError(e)
            }
        })
    }

    fun insertBroadcastChatMessageToDb(broadcastGroup: BroadcastGroup, qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage) {
        // Insert message to broadcast group
        appExecutors
                .diskIO()
                .execute {
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    val dialogId = "broadcast_" + broadcastGroup.id
                    // item.dialogId = dialogId
                    // Insert message to broadcast group
                    chatMessageDao.insert(ChatConverter.convertQbChatMessageToChatMessage(dialogId, qbChatMessage, null, currentUser))
                }
    }

    fun insertChatMessageToDb(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage, plusUnreadMessage: Boolean) {
        appExecutors
                .diskIO()
                .execute {
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    val findFriendFromId = ChatConverter.findFriendFromId(qbChatMessage.senderId, allFriends)
                    db.beginTransaction()
                    try {
                        val chatMessage = ChatConverter.convertQbChatMessageToChatMessage(qbChatDialog.dialogId, qbChatMessage, findFriendFromId, currentUser)
                        // Check system message
                        if (chatMessage?.customParam?.uxc_system_info == null) {
                            // Normal message
                            chatMessageDao.insert(chatMessage)
                            // Update dialogs
                            updateDialog(qbChatDialog, qbChatMessage, plusUnreadMessage)
                        } else {
                            // Update local message
                            val allMessages = chatMessageDao.findChatMessages(qbChatDialog.dialogId)
                            var gift: SendGiftResponse? = null
                            if (allMessages.isNotEmpty()) {
                                var chat_credit_transaction: TransferCreditResponse? = null
                                chatMessage.customParam.uxc_system_info?.let {
                                    val jsonFile: JsonObject = JsonParser().parse(it?.data).asJsonObject
                                    jsonFile?.let {
                                        if (chatMessage.customParam.uxc_system_info?.category == Constants.CATEGORY_WALLET) {
                                            chat_credit_transaction = Gson().fromJson(
                                                    jsonFile,
                                                    object : TypeToken<TransferCreditResponse>() {}.type
                                            )
                                        } else if (chatMessage.customParam.uxc_system_info?.category == Constants.CATEGORY_ECOMMERCE) {
                                            gift = Gson().fromJson(
                                                    jsonFile,
                                                    object : TypeToken<SendGiftResponse>() {}.type
                                            )
                                        }
                                    }
                                }
                                run loop@ {
                                    if (chat_credit_transaction != null) {
                                        allMessages.forEach {
                                            if (it.customParam.credit_transaction != null && chat_credit_transaction?.id == it.customParam.credit_transaction?.id) {
                                                it.customParam.credit_transaction?.status = chat_credit_transaction?.status!!
                                                chatMessageDao.insert(it)
                                                return@loop
                                            }
                                        }
                                    } else if (gift != null) {
                                        allMessages.forEach {
                                            if (it.customParam.gift != null && gift?.id == it.customParam.gift?.id) {
                                                it.customParam.gift?.status = gift?.status!!
                                                chatMessageDao.insert(it)
                                                return@loop
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
    }

    fun updateDialog(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage, plusUnreadMessage: Boolean) {
        appExecutors
                .diskIO()
                .execute {
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    // Find available dialog
                    var chatDialog = chatDialogDao?.findDialogWithId(qbChatDialog.dialogId)
                    if (chatDialog == null) {
                        chatDialogDao.insert(ChatConverter.convertQbChatDialogToChatDialog(qbChatDialog, ChatConverter.findOpponent(qbChatDialog, qbChatDialog.occupants, allFriends, currentUser)))
                    }
                    chatDialog = chatDialogDao?.findDialogWithId(qbChatDialog.dialogId)
                    // Save last message to dialog
                    qbChatMessage?.let { item ->
                        chatDialog.lastMessageText = item.body
                        chatDialog.lastMessageDate = item.dateSent?.times(1000)
                        if (plusUnreadMessage) {
                            chatDialog.unreadMessagesCount = (if (chatDialog.unreadMessagesCount != null)
                                chatDialog.unreadMessagesCount!! + 1 else 1)
                        }
                        /*else {
                            // is chatting with other user
                            chatDialog.unreadMessagesCount = 0
                        }*/
                        chatDialog.lastMessageSenderId = item.senderId
                        // custom properties
                        val properties = item?.properties
                        chatDialog.lastMessageContentType = properties?.get(Constants.CHAT_CONTENT_TYPE)
                        insertDialogToDb(chatDialog)
                        Functions.showLogMessage("Trong", "update dialog  " + chatDialog.name + " with " + qbChatMessage.id)
                    }
                }
    }

    fun deleteChatDB() {
        appExecutors
                .diskIO()
                .execute {
                    // Remove all in db
                    chatDialogDao.deleteAll()
                    chatMessageDao.deleteAll()
                }
    }

    fun deleteChatMessageById(qbChatMessage: QBChatMessage?) {
        appExecutors
                .diskIO()
                .execute {
                    qbChatMessage?.id?.let { chatMessageDao.deleteMessageId(it) }
                }
    }

    fun getFriendByIds(friendIds: String, callback: ChatCallbackData<List<Friend>>?): LiveData<Resource<List<Friend>>> {
        return object : NetworkCallResource<List<Friend>>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: List<Friend>) {
                db.beginTransaction()

                try {
                    callback?.returnResult(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<List<Friend>>> {
                return nCardService.getFriendsByIds(friendIds)
            }
        }.asLiveData()
    }

    fun getFriendsByChatIds(friendIds: String, callback: ChatCallbackData<List<Friend>>?): LiveData<Resource<List<Friend>>> {
        return object : NetworkCallResource<List<Friend>>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: List<Friend>) {
                db.beginTransaction()

                try {
                    callback?.returnResult(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<List<Friend>>> {
                return nCardService.getFriendsByChatIds(friendIds)
            }
        }.asLiveData()
    }

    fun deleteChatMessage(qbChatDialog: QBChatDialog, chatMessage: ChatMessage): LiveData<Resource<ChatMessage>> {
        return object : NetworkCallResource<ChatMessage>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: ChatMessage) {
                db.beginTransaction()

                try {
                    chatMessageDao.deleteMessageId(item.messageId!!)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<ChatMessage>> {
                return chatService.deleteChatMessage(qbChatDialog, chatMessage)
            }
        }.asLiveData()
    }

    fun updateDialogUsers(name: String, qbChatDialog: QBChatDialog, friendsList: List<Friend>): LiveData<Resource<QBChatDialog>> {
        return object : NetworkCallResource<QBChatDialog>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: QBChatDialog) {
                db.beginTransaction()

                try {
                    val allFriends = friendDao.loadAllFriends()     // Set name and avatar for sender id
                    val currentUser = userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)
                    chatDialogDao.insert(ChatConverter.convertQbChatDialogToChatDialog(item, ChatConverter.findOpponent(qbChatDialog, item.occupants, allFriends, currentUser)))
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<QBChatDialog>> {
                return chatService.updateDialogUsers(name, qbChatDialog, friendsList)
            }
        }.asLiveData()
    }

    fun exitFromDialog(qbChatDialog: QBChatDialog): LiveData<Resource<QBChatDialog>> {
        return object : NetworkCallResource<QBChatDialog>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: QBChatDialog) {
                db.beginTransaction()

                try {
                    chatDialogDao.deleteChatDialogById(qbChatDialog.dialogId)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<QBChatDialog>> {
                return chatService.exitFromDialog(qbChatDialog)
            }
        }.asLiveData()
    }

    fun getCreditTransactionDetail(chatMessage: ChatMessage, transactionId: Int): LiveData<Resource<TransferCreditResponse>> {
        return object : NetworkCallResource<TransferCreditResponse>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: TransferCreditResponse) {
                db.beginTransaction()

                try {
                    val creditTransactionLocal = chatMessage?.customParam.credit_transaction
                    if (creditTransactionLocal != null && item != null) {
                        if (creditTransactionLocal.status != item.status) {
                            chatMessage.customParam.credit_transaction = item
                            chatMessageDao.insert(chatMessage)
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<TransferCreditResponse>> {
                return nCardService.getCreditTransactionDetail(transactionId)
            }
        }.asLiveData()
    }

    fun updateCreditTransactionDetail(chatMessage: ChatMessage, transactionId: Int, body: RequestUpdateCreditTransaction): LiveData<Resource<TransferCreditResponse>> {
        return object : NetworkCallResource<TransferCreditResponse>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: TransferCreditResponse) {
                db.beginTransaction()

                try {
                    val creditTransactionLocal = chatMessage?.customParam.credit_transaction
                    if (creditTransactionLocal != null && item != null) {
                        if (creditTransactionLocal.status != item.status) {
                            chatMessage.customParam.credit_transaction = item
                            chatMessageDao.insert(chatMessage)
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<TransferCreditResponse>> {
                return nCardService.updateCreditTransaction(transactionId, body)
            }
        }.asLiveData()
    }

    fun getGiftDetail(chatMessage: ChatMessage, giftId: Int): LiveData<Resource<SendGiftResponse>> {
        return object : NetworkCallResource<SendGiftResponse>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: SendGiftResponse) {
                db.beginTransaction()

                try {
                    val giftLocal = chatMessage?.customParam.gift
                    if (giftLocal != null && item != null) {
                        if (giftLocal.status != item.status) {
                            chatMessage.customParam.gift = item
                            chatMessageDao.insert(chatMessage)
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<SendGiftResponse>> {
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                return nCardService.getGiftDetail(userId, giftId)
            }
        }.asLiveData()
    }

    fun updateGiftDetail(chatMessage: ChatMessage, transactionId: Int, body: RequestUpdateGift): LiveData<Resource<SendGiftResponse>> {
        return object : NetworkCallResource<SendGiftResponse>(appExecutors, sharedPreferenceHelper) {

            override fun returnCallResult(item: SendGiftResponse) {
                db.beginTransaction()

                try {
                    val giftLocal = chatMessage?.customParam.gift
                    if (giftLocal != null && item != null) {
                        if (giftLocal.status != item.status) {
                            chatMessage.customParam.gift = item
                            chatMessageDao.insert(chatMessage)
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun createCall(): LiveData<ApiResponse<SendGiftResponse>> {
                return nCardService.updateGift(transactionId, body)
            }
        }.asLiveData()
    }

    fun getDialogById(dialogId: String): LiveData<ChatDialog> {
        return chatDialogDao.findLiveDataDialogWithId(dialogId)
    }

    fun getUnreadChatDialog(): LiveData<List<ChatDialog>> {
        return chatDialogDao.findUnreadChatDialog()
    }

    interface ChatCallbackData<T> {
        fun returnResult(t: T)
    }

}