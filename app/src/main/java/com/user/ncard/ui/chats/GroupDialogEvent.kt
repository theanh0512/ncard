package com.user.ncard.ui.chats

import com.quickblox.chat.model.QBChatDialog
import com.user.ncard.vo.Friend

/**
 * Created by trong-android-dev on 31/10/17.
 */
data class GroupDialogEvent(val qbChatDialog: QBChatDialog, val occupantIdsRemoved: List<Int>?, val notification_type: Int) {
}