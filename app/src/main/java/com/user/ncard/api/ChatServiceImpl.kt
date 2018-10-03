package com.user.ncard.api

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.ChatHelper
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.ChatMessage
import com.user.ncard.vo.Friend
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ChatServiceImpl @Inject constructor(val chatHelper: ChatHelper) : ChatService {

    companion object {
        const val CHAT_ERROR_CODE = 550
    }

    override fun getChatDialogs(requestBuilder: QBRequestGetBuilder): LiveData<ApiResponse<ArrayList<QBChatDialog>>> {
        val result = MutableLiveData<ApiResponse<ArrayList<QBChatDialog>>>()
        chatHelper.getDialogs(requestBuilder!!, object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle) {
                result.value = ApiResponse(Response.success(dialogs))
            }

            override fun onError(e: QBResponseException) {
                // TODO: Need to handle error here
                //val builder = okhttp3.Response.Builder().message(e.message).code(123)
                Functions.showErrorLogMessage("ChatServiceImpl", e.message)
                result.value = ApiResponse(Response.error(CHAT_ERROR_CODE, ResponseBody.create(null, e.message)))
            }
        })

        return result
    }


    override fun getChatMessageFromDialog(qbChatDialog: QBChatDialog): LiveData<ApiResponse<ArrayList<QBChatMessage>>> {
        val result = MutableLiveData<ApiResponse<ArrayList<QBChatMessage>>>()
        chatHelper.getChatMessagesFromDialog(qbChatDialog, object : QBEntityCallback<ArrayList<QBChatMessage>> {
            override fun onSuccess(messages: ArrayList<QBChatMessage>, bundle: Bundle) {
                result.value = ApiResponse(Response.success(messages))
            }

            override fun onError(e: QBResponseException) {
                // TODO: Need to handle error here
                //val builder = okhttp3.Response.Builder().message(e.message).code(123)
                Functions.showErrorLogMessage("ChatServiceImpl", e.message)
                result.value = ApiResponse(Response.error(CHAT_ERROR_CODE, ResponseBody.create(null, e.message)))
            }
        })

        return result
    }

    override fun sendChatMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage): LiveData<ApiResponse<QBChatMessage>> {
        val result = MutableLiveData<ApiResponse<QBChatMessage>>()
        chatHelper.sendChatMessage(qbChatDialog, qbChatMessage, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                result.value = ApiResponse(Response.success(qbChatMessage))
            }

            override fun onError(e: QBResponseException) {
                // TODO: Need to handle error here
                //val builder = okhttp3.Response.Builder().message(e.message).code(123)
                Functions.showErrorLogMessage("ChatServiceImpl", e.message)
                result.value = ApiResponse(Response.error(CHAT_ERROR_CODE, ResponseBody.create(null, e.message)))
            }

        })
        return result
    }

    override fun sendChatMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage, callback: QBEntityCallback<Void>){
        chatHelper.sendChatMessage(qbChatDialog, qbChatMessage, callback)
    }

    override fun sendChatMessageWithoutJoined(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage): LiveData<ApiResponse<QBChatMessage>> {
        val result = MutableLiveData<ApiResponse<QBChatMessage>>()
        chatHelper.sendChatMessageWithoutJoined(qbChatDialog, qbChatMessage, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                result.value = ApiResponse(Response.success(qbChatMessage))
            }

            override fun onError(e: QBResponseException) {
                // TODO: Need to handle error here
                //val builder = okhttp3.Response.Builder().message(e.message).code(123)
                Functions.showErrorLogMessage("ChatServiceImpl", e.message)
                result.value = ApiResponse(Response.error(CHAT_ERROR_CODE, ResponseBody.create(null, e.message)))
            }

        })
        return result
    }

    override fun getBroadcastChatMessage(broadcastGroup: BroadcastGroup): LiveData<ApiResponse<ArrayList<QBChatMessage>>> {
        // Fake call
        return MutableLiveData<ApiResponse<ArrayList<QBChatMessage>>>()
    }

    override fun deleteChatMessage(qbChatDialog: QBChatDialog, chatMessage: ChatMessage): LiveData<ApiResponse<ChatMessage>> {
        val result = MutableLiveData<ApiResponse<ChatMessage>>()
        chatHelper.deleteChatMessage(qbChatDialog, chatMessage.messageId!!, object : QBEntityCallback<Void> {
            override fun onSuccess(void: Void?, bundle: Bundle?) {
                result.value = ApiResponse(Response.success(chatMessage))
            }

            override fun onError(e: QBResponseException) {
                // TODO: Need to handle error here
                //val builder = okhttp3.Response.Builder().message(e.message).code(123)
                Functions.showErrorLogMessage("ChatServiceImpl", e.message)
                // Always can delete message
                result.value = ApiResponse(Response.success(chatMessage))
                //result.value = ApiResponse(Response.error(CHAT_ERROR_CODE, ResponseBody.create(null, e.message)))
            }

        })
        return result
    }

    override fun updateDialogUsers(name: String, qbDialog: QBChatDialog, friendsList: List<Friend>): LiveData<ApiResponse<QBChatDialog>> {
        val result = MutableLiveData<ApiResponse<QBChatDialog>>()
        chatHelper.updateDialogUsers(name, qbDialog, friendsList, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog, bundle: Bundle) {
                result.value = ApiResponse(Response.success(dialog))
            }

            override fun onError(e: QBResponseException) {
                // TODO: Need to handle error here
                //val builder = okhttp3.Response.Builder().message(e.message).code(123)
                Functions.showErrorLogMessage("ChatServiceImpl", e.message)
                result.value = ApiResponse(Response.error(CHAT_ERROR_CODE, ResponseBody.create(null, e.message)))
            }
        })

        return result
    }

    override fun exitFromDialog(qbChatDialog: QBChatDialog): LiveData<ApiResponse<QBChatDialog>> {
        val result = MutableLiveData<ApiResponse<QBChatDialog>>()
        chatHelper.exitFromDialog(qbChatDialog, object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(dialog: QBChatDialog, bundle: Bundle) {
                result.value = ApiResponse(Response.success(dialog))
            }

            override fun onError(e: QBResponseException) {
                // TODO: Need to handle error here
                //val builder = okhttp3.Response.Builder().message(e.message).code(123)
                Functions.showErrorLogMessage("ChatServiceImpl", e.message)
                result.value = ApiResponse(Response.error(CHAT_ERROR_CODE, ResponseBody.create(null, e.message)))
            }
        })

        return result
    }

}
