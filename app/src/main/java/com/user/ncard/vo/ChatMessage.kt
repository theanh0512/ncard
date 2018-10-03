package com.user.ncard.vo

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by dangui on 10/11/17.
 */
@Entity(primaryKeys = arrayOf("messageId", "dialogId"))
class ChatMessage(
        var messageId: String?,
        var dialogId: String?,
        var recipientId: Int?,
        var senderId: Int?,
        var senderName: String?,
        var senderAvatar: String?,
        var text: String?,
        var messageType: String?,
        var systemMessageType: String?,
        var dateSent: Date?,

        var createdAt: Date?,
        var updatedAt: Date?,
        var isRead: Boolean?, // Only for incoming message
        @Embedded var customParam: ChatMessageCustomParam

) {
    @Ignore
    constructor(messageId: String?) : this(messageId, null, null, null, null, null, null, null, null, null, null, null, null,
            ChatMessageCustomParam(ChatMessageContentType.UNKNOWN.type,
                    null, null, null, null, null, null, null, null))


    class ChatMessageCustomParam(var chat_content_type: String, @Embedded(prefix = "location_") var chat_location: ChatLocation?,
                                 @Embedded(prefix = "file_") var chat_file: ChatFile?,
                                 @Embedded(prefix = "credit_") var credit_transaction: TransferCreditResponse?,
                                 @Embedded(prefix = "gift_") var gift: SendGiftResponse?,
                                 @Embedded(prefix = "system_") var uxc_system_info: ChatSystemInfoMessage?,
                                 var notification_type: String?,
                                 var save_to_history: String?, var sender_date_sent: Date?)

    class ChatFile(var type: String, var remoteUrl: String?, var duration: String?, var thumbnailRemoteUrl: String?)

    class ChatLocation(var lat: Double, var lng: Double, var name: String?, var address: String?)

    class ChatSystemInfoMessage(var category: String, var action: String, var data: String?)

}

/*  To Add later:

    public var attachment: UXCAttachment?
    public var location: UXCLocation?
    public var creditTransaction: EWalletTransaction?
    public var gift: Gift?
    public var internalSystemMessageInfo: UXCInternalSystemMessageInfo?

    public var delayed: Bool?
    public var markable: Bool = false

    /**
     *  The array of user's ids who received this message.
     */
    public var readIds: [UInt]?

    /**
     *  The array of user's ids who read this message.
     */
    public var deliveredIds: [UInt]?
    public var isBroadcastMsg: Bool = false
 */