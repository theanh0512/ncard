package com.user.ncard.ui.chats.utils

import android.util.SparseArray
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.users.model.QBUser
import com.user.ncard.util.Constants
import com.user.ncard.vo.*
import java.util.*
import com.google.gson.JsonParser
import com.quickblox.chat.model.QBDialogType
import kotlin.collections.ArrayList


/**
 * Created by trong-android-dev on 23/11/17.
 */
class ChatConverter {
    companion object {
        open fun convertQbChatDialogToChatDialog(qbChatDialog: QBChatDialog, friend: Friend?): ChatDialog {

            var name = ""
            if (qbChatDialog.type.code == QBDialogType.PRIVATE.code) {
                name = if (friend != null) String.format("%s %s", friend?.firstName, friend?.lastName) else ""
                if (name == "") {
                    // user not friend
                    name = "Stranger"
                }
            } else if (qbChatDialog.type.code == QBDialogType.GROUP.code) {
                name = qbChatDialog.name
            }

            val lastMessageDateSent = if (qbChatDialog.lastMessageDateSent.toInt() != 0) qbChatDialog.lastMessageDateSent?.times(1000) else qbChatDialog.createdAt.time
            return ChatDialog(qbChatDialog.dialogId, qbChatDialog.userId, null, qbChatDialog.type.code, name, if (friend == null) qbChatDialog.photo else friend.profileImageUrl,
                    qbChatDialog.lastMessage, lastMessageDateSent, ChatMessageContentType.TEXT.type, qbChatDialog.lastMessageUserId,
                    qbChatDialog.unreadMessageCount, qbChatDialog.occupants, qbChatDialog.createdAt, qbChatDialog.updatedAt, null)
        }

        open fun convertListQbChatDialogToChatDialog(list: List<QBChatDialog>, listFriends: List<Friend>?, currentUser: User?): List<ChatDialog> {
            list?.isNotEmpty().let {
                return list.map { convertQbChatDialogToChatDialog(it, findOpponent(it, it.occupants, listFriends, currentUser)) }
            }
            return arrayListOf()
        }

        open fun convertChatDialogToQbChatDialog(chatDialog: ChatDialog): QBChatDialog {

            val qbChatDialog = QBChatDialog()
            qbChatDialog.dialogId = chatDialog.dialogId!!
            qbChatDialog.userId = chatDialog.ownerUserId
            qbChatDialog.photo = chatDialog.photo
            qbChatDialog.name = chatDialog.name
            qbChatDialog.unreadMessageCount = chatDialog.unreadMessagesCount
            qbChatDialog.setOccupantsIds(chatDialog.occupantIds)
            if (chatDialog.type == QBDialogType.PRIVATE.code) {
                qbChatDialog.type = QBDialogType.PRIVATE
            } else if (chatDialog.type == QBDialogType.GROUP.code) {
                qbChatDialog.type = QBDialogType.GROUP
            }
            qbChatDialog.createdAt = chatDialog.createdAt
            qbChatDialog.updatedAt = chatDialog.updatedAt
            qbChatDialog.lastMessage = chatDialog.lastMessageText
            qbChatDialog.lastMessageDateSent = chatDialog.lastMessageDate!!.div(1000)
            qbChatDialog.lastMessageUserId = chatDialog.lastMessageSenderId

            return qbChatDialog
        }

        open fun convertListChatDialogToQbChatDialog(list: List<ChatDialog>): List<QBChatDialog> {
            list?.isNotEmpty().let {
                return list.map { convertChatDialogToQbChatDialog(it) }
            }
            return arrayListOf()
        }

        private fun convertQbUserToChatUser(qbUser: QBUser): ChatUser {
            val chatUser = ChatUser(qbUser.id)
            chatUser.userId = qbUser.id
            chatUser.fullName = qbUser.fullName
            chatUser.email = qbUser.email
            chatUser.login = qbUser.login
            chatUser.phone = qbUser.phone
            chatUser.website = qbUser.website
            chatUser.lastRequestAt = qbUser.lastRequestAt
            chatUser.data = qbUser.customData

            return chatUser
        }

        open fun convertListQbUserToChatUser(list: List<QBUser>): List<ChatUser> {
            list?.isNotEmpty().let {
                return list.map { convertQbUserToChatUser(it) }
            }
            return arrayListOf()
        }

        open fun convertListQbUserToChatUser(list: SparseArray<QBUser>): List<ChatUser> {
            var result: MutableList<ChatUser> = ArrayList<ChatUser>()
            if (list != null && list.size() > 0) {
                for (i in 0 until list.size()!!) {
                    result.add(convertQbUserToChatUser(list.valueAt(i)))
                }
            }
            return result
        }

        fun convertFriendToBroadcastMember(friend: Friend): BroadcastGroup.Member {
            friend?.let {
                return BroadcastGroup.Member(friend?.id, friend?.username, friend?.firstName, friend?.lastName, friend?.email,
                        friend?.profileImageUrl, friend?.thumbnailUrl)
            }
            return BroadcastGroup.Member(-1, null, null, null, null, null, null)
        }

        open fun convertListFriendsToBroadcastMember(list: List<Friend>): List<BroadcastGroup.Member> {
            list?.isNotEmpty().let {
                return list.map { convertFriendToBroadcastMember(it) }
            }
            return arrayListOf()
        }


        open fun convertQbChatMessageToChatMessage(dialogId: String?, qbChatMessage: QBChatMessage?, friend: Friend?, currentUser: User?): ChatMessage {
            if (dialogId == null || qbChatMessage == null) {
                return ChatMessage("-1")
            }

            val messageId = qbChatMessage.id
            val recipientId = qbChatMessage.recipientId
            val senderId = qbChatMessage.senderId
            //TODO: sender name not in qbChatMessage, get the full name by senderId from friend repository or group chat participant repository
            var senderName = ""
            var senderAvatar: String? = null
            if (currentUser?.chatId == senderId) {
                senderName = String.format("%s %s", currentUser?.firstName, currentUser?.lastName)
                senderAvatar = currentUser?.profileImageUrl
            } else {
                senderName = if (friend != null) String.format("%s %s", friend?.firstName, friend?.lastName) else ""
                senderAvatar = friend?.profileImageUrl
            }
            if (senderName == "") {
                // user not friend
                senderName = "Stranger"
            }
            // custom properties
            val properties = qbChatMessage?.properties
            val chat_content_type = properties?.get(Constants.CHAT_CONTENT_TYPE)
            val chat_location_str = properties?.get(Constants.CHAT_LOCATION)
            val chat_file_str = properties?.get(Constants.CHAT_FILE)
            val chat_credit_str = properties?.get(Constants.CHAT_CREDIT_TRANSACTION)
            val chat_gift_str = properties?.get(Constants.CHAT_GIFT)
            val chat_system_info_str = properties?.get(Constants.CHAT_SYSTEM_INFO)
            val system_message_type = properties?.get(Constants.SYSTEM_MESSAGE_TYPE)
            val save_to_history = properties?.get(Constants.SAVE_TO_HISTORY)
            val sender_date_sent = properties?.get(Constants.SENDER_DATE_SENT)

            val dateSentTimestamp = sender_date_sent?.toDouble()?.times(1000)?.toLong()
            val dateSent = dateSentTimestamp?.let { Date(it) }

            var chat_location: ChatMessage.ChatLocation? = null
            chat_location_str?.let {
                val jsonLocation: JsonObject? = JsonParser().parse(chat_location_str)?.asJsonObject
                jsonLocation?.let {
                    chat_location = Gson().fromJson(
                            jsonLocation,
                            object : TypeToken<ChatMessage.ChatLocation>() {}.type
                    )
                }
            }
            var chat_file: ChatMessage.ChatFile? = null
            chat_file_str?.let {
                val jsonFile: JsonObject? = JsonParser().parse(chat_file_str)?.asJsonObject
                jsonFile?.let {
                    chat_file = Gson().fromJson(
                            jsonFile,
                            object : TypeToken<ChatMessage.ChatFile>() {}.type
                    )
                }
            }

            var chat_credit_transaction: TransferCreditResponse? = null
            chat_credit_str?.let {
                val jsonFile: JsonObject? = JsonParser().parse(chat_credit_str)?.asJsonObject
                jsonFile?.let {
                    chat_credit_transaction = Gson().fromJson(
                            jsonFile,
                            object : TypeToken<TransferCreditResponse>() {}.type
                    )
                }
            }

            var chat_gift: SendGiftResponse? = null
            chat_gift_str?.let {
                val jsonFile: JsonObject? = JsonParser().parse(chat_gift_str)?.asJsonObject
                jsonFile?.let {
                    chat_gift = Gson().fromJson(
                            jsonFile,
                            object : TypeToken<SendGiftResponse>() {}.type
                    )
                }
            }

            var chat_system_info: ChatMessage.ChatSystemInfoMessage? = null
            chat_system_info_str?.let {
                val jsonFile: JsonObject? = JsonParser().parse(chat_system_info_str)?.asJsonObject
                jsonFile?.let {
                    chat_system_info = Gson().fromJson(
                            jsonFile,
                            object : TypeToken<ChatMessage.ChatSystemInfoMessage>() {}.type
                    )
                }
            }

            return ChatMessage(messageId, dialogId, recipientId, senderId, senderName, senderAvatar,
                    if (qbChatMessage.body.isNullOrBlank() || (qbChatMessage.body != null && qbChatMessage.body == "null")) null else qbChatMessage.body, //Don't know why text is "null"
                    ChatMessageType.NORMAL.type,
                    ChatSystemMessageType.UNKNOWN.type,
                    dateSent, dateSent, dateSent, false,
                    ChatMessage.ChatMessageCustomParam(chat_content_type!!, chat_location,
                            chat_file, chat_credit_transaction, chat_gift, chat_system_info, system_message_type, save_to_history,
                            dateSent))
        }

        open fun convertListQbChatMessageToChatMessage(dialogId: String?, list: List<QBChatMessage>,
                                                       listFriends: List<Friend>?,
                                                       currentUser: User?): List<ChatMessage> {
            list?.isNotEmpty().let {
                return list.map { convertQbChatMessageToChatMessage(dialogId, it, findFriendFromId(it.senderId, listFriends), currentUser) }
            }
            return arrayListOf()
        }

        open fun findFriendFromId(senderId: Int, listFriends: List<Friend>?): Friend? {
            listFriends?.isNotEmpty().let {
                listFriends?.forEach {
                    if (it.chatId == senderId) {
                        return it
                    }
                }
            }
            return null
        }

        open fun findOpponent(qbChatDialog: QBChatDialog, occupantIds: List<Int>?, listFriends: List<Friend>?, currentUser: User?): Friend? {
            if (qbChatDialog?.type == QBDialogType.PRIVATE && occupantIds != null && occupantIds.size == 2) { // Private chat
                val opponentId = occupantIds?.filter { it != currentUser?.chatId }
                if (opponentId != null && opponentId.size == 1) {
                    val findFriendFromId = findFriendFromId(opponentId.get(0), listFriends)
                    return findFriendFromId
                }
            }
            return null
        }

    }
}