package com.user.ncard.ui.chats.utils

import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import com.google.gson.Gson
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBPrivacyListsManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.model.*
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.user.ncard.repository.ChatRepository
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.GroupDialogUpdateEvent
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.*
import org.greenrobot.eventbus.EventBus
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import javax.inject.Inject
import kotlin.Comparator
import kotlin.collections.ArrayList


/**
 * Created by trong-android-dev on 25/12/17.
 */
class DialogManager @Inject constructor(val context: Context, val chatHelper: ChatHelper, val chatRepository: ChatRepository,
                                        val sharedPreferenceHelper: SharedPreferenceHelper) {

    private val TAG = "DialogManager"

    private val managingDialogsCallbackListener = CopyOnWriteArraySet<ManagingDialogsCallbacks>()

    fun onGlobalMessageReceived(dialogId: String, qbChatMessage: QBChatMessage, senderId: Int?) {
        object : AsyncTask<Void, Void, ChatDialog?>() {
            override fun doInBackground(vararg params: Void): ChatDialog? {
                return chatRepository?.chatDialogDao.findDialogWithId(dialogId)
            }

            override fun onPostExecute(chatDialog: ChatDialog?) {
                if (chatDialog == null) {
                    chatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                        override fun onError(qbResponseException: QBResponseException?) {
                        }

                        override fun onSuccess(qbChatDialog: QBChatDialog?, bundle: Bundle?) {
                            qbChatDialog?.let {
                                chatRepository.insertDialogToDb(qbChatDialog)
                            }
                        }
                    })
                } else {
                    /*// Save last message to dialog
                    qbChatMessage?.let {
                        chatDialog.lastMessageText = qbChatMessage.body
                        chatDialog.lastMessageDate = qbChatMessage.dateSent?.times(1000)
                        chatDialog.unreadMessagesCount = (if (chatDialog.unreadMessagesCount != null)
                            chatDialog.unreadMessagesCount!! + 1 else 1)
                        chatDialog.lastMessageSenderId = qbChatMessage.senderId
                        // custom properties
                        val properties = qbChatMessage?.properties
                        chatDialog.lastMessageContentType = properties?.get(Constants.CHAT_CONTENT_TYPE)
                        chatRepository.insertDialogToDb(chatDialog)
                        chatRepository.insertChatMessageToDb(ChatConverter.convertChatDialogToQbChatDialog(chatDialog), qbChatMessage)
                    }*/
                    chatRepository.updateDialog(ChatConverter.convertChatDialogToQbChatDialog(chatDialog), qbChatMessage, true)

                }
            }
        }.execute()
    }

    private fun isMessageCreatingDialog(systemMessage: QBChatMessage): Boolean {
        return QMMessageType.createGroupDialog.type == (systemMessage.getProperty(Constants.SYSTEM_MESSAGE_TYPE) as String).toInt()
    }

    private fun isMessageUpdatingDialog(systemMessage: QBChatMessage): Boolean {
        return QMMessageType.updateGroupDialog.type == (systemMessage.getProperty(Constants.SYSTEM_MESSAGE_TYPE) as String).toInt()
    }

    private fun buildChatDialogFromSystemMessage(qbChatMessage: QBChatMessage): QBChatDialog {
        val chatDialog = QBChatDialog()
        val dialogId: String? = if (qbChatMessage.dialogId != "null") qbChatMessage?.dialogId else qbChatMessage.getProperty(Constants.PROPERTY_DIALOG_ID).toString()
        chatDialog.dialogId = dialogId
        chatDialog.setOccupantsIds(getOccupantsIdsListFromString(qbChatMessage.getProperty(Constants.PROPERTY_OCCUPANTS_IDS) as String))
        chatDialog.type = QBDialogType.parseByCode(Integer.parseInt(qbChatMessage.getProperty(Constants.PROPERTY_DIALOG_TYPE).toString()))
        chatDialog.name = qbChatMessage.getProperty(Constants.PROPERTY_DIALOG_NAME).toString()
        chatDialog.unreadMessageCount = 0

        return chatDialog
    }

    fun onSystemMessageReceived(systemMessage: QBChatMessage) {
        val dialogId: String? = if (systemMessage.dialogId != "null") systemMessage?.dialogId else systemMessage.getProperty(Constants.PROPERTY_DIALOG_ID).toString()
        val occupantsIds: List<Int> = getOccupantsIdsListFromString(systemMessage.getProperty(Constants.PROPERTY_OCCUPANTS_IDS) as String)
        if (isMessageCreatingDialog(systemMessage) && dialogId != null) {
            /*val chatDialog = buildChatDialogFromSystemMessage(systemMessage)
            chatDialog.initForChat(QBChatService.getInstance())
            // Save dialog
            chatRepository.insertDialogToDb(chatDialog)
            notifyListenersDialogCreated(chatDialog)*/
            chatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                override fun onError(qbResponseException: QBResponseException?) {
                    qbResponseException?.printStackTrace()
                }

                override fun onSuccess(qbChatDialog: QBChatDialog?, bundle: Bundle?) {
                    qbChatDialog?.let {
                        // Save dialog
                        chatRepository.insertDialogToDb(qbChatDialog)
                    }
                }
            })
        } else if (isMessageUpdatingDialog(systemMessage) && dialogId != null) {
            if (occupantsIds.contains(Functions.getMyChatId(sharedPreferenceHelper))) {
                chatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onError(qbResponseException: QBResponseException?) {
                        qbResponseException?.printStackTrace()
                    }

                    override fun onSuccess(qbChatDialog: QBChatDialog?, bundle: Bundle?) {
                        qbChatDialog?.let {
                            if (qbChatDialog.occupants != null && qbChatDialog.occupants.contains(Functions.getMyChatId(sharedPreferenceHelper))) {
                                // Save dialog
                                chatRepository.insertDialogToDb(qbChatDialog)
                            }
                            /*else {
                                // Another person remove me out of the group
                                chatRepository.deleteDialog(dialogId)
                            }*/
                        }
                    }
                })
            } else {
                // Another person remove me out of the group
                Toast.makeText(context, "You have been remove out of group by " + systemMessage.properties.get(Constants.PROPERTY_ACTION_MAKERNAME), Toast.LENGTH_LONG).show()
                chatRepository.deleteDialog(dialogId)
            }
        }
    }

    // Process chat message from push notification (dialog list screen): group chat cannot receive message when not openning chat group detail
    fun updateDialog(dialogId: String) {
        chatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
            override fun onError(qbResponseException: QBResponseException?) {
                qbResponseException?.printStackTrace()
            }

            override fun onSuccess(qbChatDialog: QBChatDialog?, bundle: Bundle?) {
                qbChatDialog?.let {
                    // Save dialog
                    chatRepository.insertDialogToDb(qbChatDialog)
                    // TODO: Live data Group dialog list not updated, don't know why,  work around here: update manually
                    // https://stackoverflow.com/questions/44742445/room-livedata-observer-does-not-trigger-when-database-is-updated
                    Handler().postDelayed({
                        EventBus.getDefault().post(GroupDialogUpdateEvent(qbChatDialog))
                    }, 200)

                }
            }
        })

    }


    private fun notifyListenersDialogCreated(chatDialog: QBChatDialog) {
        for (listener in getManagingDialogsCallbackListeners()) {
            listener.onDialogCreated(chatDialog)
        }
    }

    fun getManagingDialogsCallbackListeners(): Collection<ManagingDialogsCallbacks> {
        return Collections.unmodifiableCollection(managingDialogsCallbackListener)
    }


    // Send system message create dialog
    fun sendSystemMessageAboutCreatingDialog(systemMessagesManager: QBSystemMessagesManager, dialog: QBChatDialog) {
        val systemMessageCreatingDialog = buildSystemMessageAboutCreatingGroupDialog(dialog)

        try {
            for (recipientId in dialog.occupants) {
                if (recipientId != QBChatService.getInstance().user.id) {
                    systemMessageCreatingDialog.recipientId = recipientId
                    systemMessagesManager.sendSystemMessage(systemMessageCreatingDialog)
                }
            }
        } catch (e: SmackException.NotConnectedException) {
            e.printStackTrace()
        }
    }

    private fun buildSystemMessageAboutCreatingGroupDialog(dialog: QBChatDialog): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        val dateSent = Date().time / 1000
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.dateSent = dateSent
        qbChatMessage.isMarkable = false
        qbChatMessage.setSaveToHistory(false)
        //qbChatMessage.senderId = Functions.getMyChatId(sharedPreferenceHelper)
        // custom params
        qbChatMessage.setProperty(Constants.PROPERTY_ACTION_MAKERNAME, Functions.getFullName(sharedPreferenceHelper))
        qbChatMessage.setProperty(Constants.PROPERTY_OCCUPANTS_IDS, getOccupantsIdsStringFromList(dialog.occupants))
        qbChatMessage.setProperty(Constants.PROPERTY_DIALOG_TYPE, dialog.type.code.toString())
        qbChatMessage.setProperty(Constants.PROPERTY_DIALOG_NAME, dialog.name.toString())
        qbChatMessage.setProperty(Constants.SYSTEM_MESSAGE_TYPE, QMMessageType.createGroupDialog.type.toString())
        qbChatMessage.setProperty(Constants.PROPERTY_DATE_SENT, dateSent.toString())
        qbChatMessage.setProperty(Constants.PROPERTY_ROOM_PHOTO, dialog.photo)

        return qbChatMessage
    }

    // Send system message update dialog
    fun sendSystemMessageAboutUpdatingDialog(systemMessagesManager: QBSystemMessagesManager, dialog: QBChatDialog, occupantIdsRemoved: List<Int>?) {
        val systemMessageCreatingDialog = buildSystemMessageAboutUpdatingGroupDialog(dialog)
        try {
            for (recipientId in dialog.occupants) {
                if (recipientId != QBChatService.getInstance().user.id) {
                    systemMessageCreatingDialog.recipientId = recipientId
                    systemMessagesManager.sendSystemMessage(systemMessageCreatingDialog)
                }
            }
            if (occupantIdsRemoved != null && occupantIdsRemoved?.isNotEmpty()) {
                for (recipientId in occupantIdsRemoved) {
                    if (recipientId != QBChatService.getInstance().user.id) {
                        systemMessageCreatingDialog.recipientId = recipientId
                        systemMessagesManager.sendSystemMessage(systemMessageCreatingDialog)
                    }
                }
            }
        } catch (e: SmackException.NotConnectedException) {
            e.printStackTrace()
        }
    }

    private fun buildSystemMessageAboutUpdatingGroupDialog(dialog: QBChatDialog): QBChatMessage {
        val qbChatMessage = QBChatMessage()
        val dateSent = Date().time / 1000
        qbChatMessage.dialogId = dialog.dialogId
        qbChatMessage.dateSent = dateSent
        qbChatMessage.isMarkable = true
        qbChatMessage.setSaveToHistory(false)
        //qbChatMessage.senderId = Functions.getMyChatId(sharedPreferenceHelper)
        // custom params
        qbChatMessage.setProperty(Constants.PROPERTY_ACTION_MAKERNAME, Functions.getFullName(sharedPreferenceHelper))
        qbChatMessage.setProperty(Constants.PROPERTY_OCCUPANTS_IDS, getOccupantsIdsStringFromList(dialog.occupants))
        qbChatMessage.setProperty(Constants.PROPERTY_DIALOG_TYPE, dialog.type.code.toString())
        qbChatMessage.setProperty(Constants.PROPERTY_DIALOG_NAME, dialog.name.toString())
        qbChatMessage.setProperty(Constants.SYSTEM_MESSAGE_TYPE, QMMessageType.updateGroupDialog.type.toString())
        qbChatMessage.setProperty(Constants.PROPERTY_DATE_SENT, dateSent.toString())
        qbChatMessage.setProperty(Constants.PROPERTY_ROOM_PHOTO, dialog.photo)

        return qbChatMessage
    }

    fun getOccupantsIdsListFromString(occupantIds: String): List<Int> {
        val occupantIdsArray: Array<String> = occupantIds.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return occupantIdsArray.map { it.trim().toInt() }
    }

    fun getOccupantsIdsStringFromList(occupantIdsList: Collection<Int>): String {
        return TextUtils.join(",", occupantIdsList)
    }

    fun blocUnblockUser(privacyListManager: QBPrivacyListsManager, listPrivacy: List<QBPrivacyList>?, chatUserId: String, isAllow: Boolean) {
        var list: QBPrivacyList? = null
        var items: MutableList<QBPrivacyListItem> = ArrayList<QBPrivacyListItem>()
        if (listPrivacy != null && listPrivacy.isNotEmpty()) {
            val listFilter = listPrivacy?.filter { it.isDefaultList }
            if (listFilter != null && listFilter.isNotEmpty()) {
                list = listFilter.get(0)
            }
            (list == null).let {
                list = listPrivacy[0]
            }
            var hasChange = false
            if (list?.items != null && list?.items?.isNotEmpty()!!) {
                // Find QBPrivacyListItem has valueForType = chatUserId
                // Remove redudant value
                val set = TreeSet<QBPrivacyListItem>(Comparator<QBPrivacyListItem> { o1, o2 ->
                    if (o1.valueForType == o2.valueForType) {
                        0
                    } else 1
                })
                set.addAll(list?.items!!)
                items.addAll(set)
                run breaker@ {
                    items.forEach {
                        if (it.valueForType.contains(chatUserId)) {
                            it.isAllow = isAllow
                            hasChange = true
                            it.isMutualBlock = !isAllow
                            return@breaker
                        }
                    }
                }
            }
            if (!hasChange && isAllow == false) {
                items.add(createQBPrivacyListItem(chatUserId, isAllow))
            }
            // Edit this listPrivacy
            try {
                privacyListManager.declineActiveList()
                privacyListManager.declineDefaultList()
            } catch (e: SmackException.NotConnectedException) {
                e.printStackTrace()
            } catch (e: XMPPException.XMPPErrorException) {
                e.printStackTrace()
            } catch (e: SmackException.NoResponseException) {
                e.printStackTrace()
            }
        } else {
            // Add new listPrivacy for isAllow = false
            if (isAllow == false) {
                list = QBPrivacyList()
                list?.name = "personal"
                items.add(createQBPrivacyListItem(chatUserId, isAllow))
            }
        }
        list?.items = items

        if (list != null) {
            try {
                privacyListManager.setPrivacyList(list)
                privacyListManager.setPrivacyListAsDefault(list?.name)
                // privacyListManager.setPrivacyListAsActive(list.name)
            } catch (e: SmackException.NotConnectedException) {
                e.printStackTrace()
            } catch (e: XMPPException.XMPPErrorException) {
                e.printStackTrace()
            } catch (e: SmackException.NoResponseException) {
                e.printStackTrace()
            }
        }

    }

    fun createQBPrivacyListItem(chatUserId: String, isAllow: Boolean): QBPrivacyListItem {
        val item = QBPrivacyListItem()
        item.isAllow = isAllow
        item.type = QBPrivacyListItem.Type.USER_ID
        item.valueForType = chatUserId
        item.isMutualBlock = !isAllow

        return item
    }

    fun processTransferCredit(transferCreditResponse: TransferCreditResponse) {
        // Check dialog between receiver and me
        var currentUser: User? = null
        var allFriends: List<Friend>? = null
        run {
            object : AsyncTask<Void, Void, List<ChatDialog>?>() {
                override fun doInBackground(vararg params: Void): List<ChatDialog>? {
                    allFriends = chatRepository?.friendDao.loadAllFriends()     // Set name and avatar for sender id
                    currentUser = chatRepository?.userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)

                    val listAllPrivateChatDialogs: MutableList<ChatDialog> = chatRepository?.chatDialogDao.findAllPrivateDialogs() as MutableList<ChatDialog>
                    return listAllPrivateChatDialogs
                }

                override fun onPostExecute(listAllPrivateChatDialogs: List<ChatDialog>?) {
                    // Check private dialog has occupants id between memberid and me locally
                    var chatDialog: ChatDialog? = null
                    var recipientId = findChatIdFromUserId(allFriends, transferCreditResponse.receiver.id)
                    run loop@ {
                        listAllPrivateChatDialogs?.forEach {
                            if (it.occupantIds?.containsAll(arrayListOf(recipientId, Functions.getMyChatId(sharedPreferenceHelper)))!!) {
                                chatDialog = it
                                return@loop
                            }
                        }
                    }
                    if (chatDialog != null) {
                        val qbChatDialog = ChatConverter.convertChatDialogToQbChatDialog(chatDialog!!)
                        qbChatDialog.initForChat(QBChatService.getInstance())
                        sendChatMessage(qbChatDialog, null, ChatMessageContentType.CREDIT.type, null, null, Gson().toJson(transferCreditResponse), null)
                    } else {
                        //create chat dialog
                        chatHelper.createPrivateChatDialog(recipientId, object : ChatHelper.CreateChatDialogListener {
                            override fun onCreateSuccess(qbChatDialog: QBChatDialog) {
                                chatRepository?.insertDialogToDb(qbChatDialog)
                                sendChatMessage(qbChatDialog, null, ChatMessageContentType.CREDIT.type, null, null, Gson().toJson(transferCreditResponse), null)
                            }

                            override fun onCreateError() {

                            }
                        })
                    }
                }
            }.execute()
        }
    }

    fun processSendGift(sendGiftResponse: SendGiftResponse) {
        // Check dialog between receiver and me
        var currentUser: User? = null
        var allFriends: List<Friend>? = null
        run {
            object : AsyncTask<Void, Void, List<ChatDialog>?>() {
                override fun doInBackground(vararg params: Void): List<ChatDialog>? {
                    allFriends = chatRepository?.friendDao.loadAllFriends()     // Set name and avatar for sender id
                    currentUser = chatRepository?.userDao.loadByUserId(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
                    currentUser?.chatId = Functions.getMyChatId(sharedPreferenceHelper)

                    val listAllPrivateChatDialogs: MutableList<ChatDialog> = chatRepository?.chatDialogDao.findAllPrivateDialogs() as MutableList<ChatDialog>
                    return listAllPrivateChatDialogs
                }

                override fun onPostExecute(listAllPrivateChatDialogs: List<ChatDialog>?) {
                    // Check private dialog has occupants id between memberid and me locally
                    var chatDialog: ChatDialog? = null
                    var recipientId = findChatIdFromUserId(allFriends, sendGiftResponse.receiver.id!!)
                    run loop@ {
                        listAllPrivateChatDialogs?.forEach {
                            if (it.occupantIds?.containsAll(arrayListOf(recipientId, Functions.getMyChatId(sharedPreferenceHelper)))!!) {
                                chatDialog = it
                                return@loop
                            }
                        }
                    }
                    if (chatDialog != null) {
                        val qbChatDialog = ChatConverter.convertChatDialogToQbChatDialog(chatDialog!!)
                        qbChatDialog.initForChat(QBChatService.getInstance())
                        sendChatMessage(qbChatDialog, null, ChatMessageContentType.GIFT.type, null, null, null, Gson().toJson(sendGiftResponse))
                    } else {
                        //create chat dialog
                        chatHelper.createPrivateChatDialog(recipientId, object : ChatHelper.CreateChatDialogListener {
                            override fun onCreateSuccess(qbChatDialog: QBChatDialog) {
                                chatRepository?.insertDialogToDb(qbChatDialog)
                                sendChatMessage(qbChatDialog, null, ChatMessageContentType.GIFT.type, null, null, null, Gson().toJson(sendGiftResponse))
                            }

                            override fun onCreateError() {

                            }
                        })
                    }
                }
            }.execute()
        }
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

    fun sendChatMessage(qbChatDialog: QBChatDialog, text: String?, type: String, chat_file: String?, chat_location: String?, credit_transaction: String?, gift: String?) {
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
            credit_transaction?.let { qbChatMessage.setProperty(Constants.CHAT_CREDIT_TRANSACTION, credit_transaction) }
            gift?.let { qbChatMessage.setProperty(Constants.CHAT_GIFT, gift) }

            // Send and save message credit transfer
            chatRepository.sendAndSaveChatMessage(qbChatDialog, qbChatMessage, object : QBEntityCallback<Void> {
                override fun onError(error: QBResponseException?) {
                    //Functions.showToastShortMessage("")
                }

                override fun onSuccess(p0: Void?, p1: Bundle?) {
                }

            })

        } catch (e: SmackException.NotConnectedException) {
            Utils.Log(TAG, e.localizedMessage)
        }
    }

    fun sendChatGift() {

    }

    interface ManagingDialogsCallbacks {

        fun onDialogCreated(chatDialog: QBChatDialog)

        fun onDialogUpdated(chatDialog: String)

        fun onNewDialogLoaded(chatDialog: QBChatDialog)
    }
}

