package com.user.ncard.api

import android.arch.lifecycle.LiveData
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.request.QBRequestGetBuilder
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.ChatMessage
import com.user.ncard.vo.Friend

/**
 * Created by trong-android-dev on 22/11/17.
 */

interface ChatService {

    fun getChatDialogs(requestBuilder: QBRequestGetBuilder): LiveData<ApiResponse<ArrayList<QBChatDialog>>>

    fun getChatMessageFromDialog(qbChatDialog: QBChatDialog): LiveData<ApiResponse<ArrayList<QBChatMessage>>>

    fun sendChatMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage): LiveData<ApiResponse<QBChatMessage>>

    fun sendChatMessage(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage, callback: QBEntityCallback<Void>)

    fun sendChatMessageWithoutJoined(qbChatDialog: QBChatDialog, qbChatMessage: QBChatMessage): LiveData<ApiResponse<QBChatMessage>>

    fun getBroadcastChatMessage(broadcastGroup: BroadcastGroup): LiveData<ApiResponse<ArrayList<QBChatMessage>>>

    fun deleteChatMessage(qbChatDialog: QBChatDialog, chatMessage: ChatMessage): LiveData<ApiResponse<ChatMessage>>

    fun updateDialogUsers(name: String,
                          qbDialog: QBChatDialog,
                          friendsList: List<Friend>): LiveData<ApiResponse<QBChatDialog>>

    fun exitFromDialog(qbChatDialog: QBChatDialog): LiveData<ApiResponse<QBChatDialog>>

}