package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * Created by dangui on 10/11/17.
 */

@Entity
class ChatDialog(
        @PrimaryKey var dialogId: String?,
        var ownerUserId: Int?,
        var recipientId: Int?, // For private chat only
        var type: Int?, // enum ChatDialogType
        var name: String?,
        var photo: String?,
        var lastMessageText: String?,
        var lastMessageDate: Long?,
        var lastMessageContentType: String?, // enum ChatMessageContentType
        var lastMessageSenderId: Int?,
        var unreadMessagesCount: Int? = 0,
        var occupantIds: List<Int>?,
        var createdAt: Date?,
        var updatedAt: Date?,
        var data: String? //extra custom parameters
) : Serializable {
    @Ignore
    constructor(dialogId: String?) : this(dialogId, null, null, null, null, null, null, null, null, null, null, null, null, null, null)

}