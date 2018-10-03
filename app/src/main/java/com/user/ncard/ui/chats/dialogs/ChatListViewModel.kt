package com.user.ncard.ui.chats.dialogs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.BroadcastReceiver
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import com.quickblox.chat.QBIncomingMessagesManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.catalogue.utils.ResourcePaging
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.ChatDialog
import java.util.*
import javax.inject.Inject


/**
 * Created by trong-android-dev on 22/11/17.
 */

class ChatListViewModel @Inject constructor(val userRepository: UserRepository,
                                            val chatRepository: ChatRepository,
                                            val sharedPreferenceHelper: SharedPreferenceHelper,
                                            var chatHelper: ChatHelper) : BaseViewModel() {

    val start = MutableLiveData<Boolean>()
    lateinit var items: LiveData<ResourcePaging<List<ChatDialog>>>
    var refresh = false;
    var forceLoad = false
    var page = DEFAULT_PAGE
    var isLoading = false
    var pagination: Pagination? = null

    val requestBuilder = QBRequestGetBuilder()

    /*lateinit var allDialogsMessagesListener: QBChatDialogMessageListener
    lateinit var incomingMessagesManager: QBIncomingMessagesManager
    lateinit var systemMessagesListener: SystemMessagesListener
    lateinit var systemMessagesManager: QBSystemMessagesManager*/

    init {
        forceLoad = false
        initData()
    }

    override fun initData() {
        items = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<ResourcePaging<List<ChatDialog>>>()
            }
            isLoading = true
            return@switchMap chatRepository.getChatDialogs(requestBuilder, refresh, forceLoad, page)
        }
    }

    fun getItems(): List<ChatDialog> {
        if (items != null && items?.value != null && items?.value?.data != null) {
            return items?.value?.data!!

        }
        return Collections.emptyList<ChatDialog>()
    }

    /*fun initListener() {
        allDialogsMessagesListener = AllDialogsMessageListener()
        systemMessagesListener = SystemMessagesListener()

        registerListener()

    }

    fun registerListener() {
        incomingMessagesManager = chatHelper.getCurrentChatService().incomingMessagesManager
        incomingMessagesManager?.addDialogMessageListener(if (allDialogsMessagesListener != null)
            allDialogsMessagesListener else AllDialogsMessageListener())

        systemMessagesManager = chatHelper.getCurrentChatService().systemMessagesManager
        systemMessagesManager.addSystemMessageListener(if (systemMessagesListener != null)
            systemMessagesListener else SystemMessagesListener())
    }

    fun unregisterListener() {
        incomingMessagesManager?.removeDialogMessageListrener(allDialogsMessagesListener)
        systemMessagesManager?.removeSystemMessageListener(systemMessagesListener)
    }

    inner class AllDialogsMessageListener : QBChatDialogMessageListener {
        override fun processError(s: String, e: QBChatException, qbChatMessage: QBChatMessage, integer: Int?) {
        }

        override fun processMessage(dialogId: String, qbChatMessage: QBChatMessage, senderId: Int?) {
            if (senderId != chatHelper.getCurrentQBUser().id) {
                // Process dialog ID and message receive
                Functions.showLogMessage("AllDialogsMessageListener", "AllDialogsMessageListener " + qbChatMessage.toString())
                onGlobalMessageReceived(dialogId, qbChatMessage, senderId)
            }
        }
    }

    inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            Functions.showLogMessage("SystemMessagesListener", "SystemMessagesListener " + qbChatMessage.toString())
        }

        override fun processError(e: QBChatException, qbChatMessage: QBChatMessage) {

        }
    }

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
                    // Save last message to dialog
                    *//*qbChatMessage?.let {
                        chatDialog.lastMessageText = qbChatMessage.body
                        chatDialog.lastMessageDate = qbChatMessage.dateSent?.times(1000)
                        chatDialog.unreadMessagesCount = (if (chatDialog.unreadMessagesCount != null)
                            chatDialog.unreadMessagesCount!! + 1 else 1)
                        chatDialog.lastMessageSenderId = qbChatMessage.senderId
                        // custom properties
                        val properties = qbChatMessage?.properties
                        chatDialog.lastMessageContentType = properties?.get(Constants.CHAT_CONTENT_TYPE)
                        chatRepository.insertDialogToDb(chatDialog)
                    }*//*
                    chatRepository.updateDialog(ChatConverter.convertChatDialogToQbChatDialog(chatDialog), qbChatMessage, false)
                }
            }
        }.execute()
    }*/

    fun updateDialog(dialogId: String) {
        chatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
            override fun onError(qbResponseException: QBResponseException?) {

            }

            override fun onSuccess(qbChatDialog: QBChatDialog?, bundle: Bundle?) {
                qbChatDialog?.let {
                    chatRepository.insertDialogToDb(qbChatDialog)
                }
            }
        })
    }

    fun reLoadFromDb() {
        refresh = false
        forceLoad = false
        page = DEFAULT_PAGE

        start.value = true
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