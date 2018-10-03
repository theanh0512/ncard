package com.user.ncard.ui.chats.dialogs

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.quickblox.chat.model.QBDialogType
import com.user.ncard.R
import com.user.ncard.databinding.ItemChatDialogBinding
import com.user.ncard.ui.card.catalogue.main.CatalogueAdapter
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.ui.catalogue.views.RelativeTimeTextView
import com.user.ncard.ui.chats.quickblox.QbUsersHolder
import com.user.ncard.util.ChatHelper
import com.user.ncard.vo.ChatDialog
import com.user.ncard.vo.ChatMessageContentType

/**
 * Created by Concaro on 7/17/2017.
 */
class ChatDialogsAdapter(val ctx: Context,
                         var items: List<ChatDialog>,
                         val chatHelper: ChatHelper,
                         val listener: OnItemClickListener) : DefaultEndlessRecyclerAdapter<ChatDialogsAdapter.ItemViewHolder>() {

    companion object {
        var listener: OnItemClickListener? = null
    }

    fun updateItems(items: List<ChatDialog>) {
        if (items == null) {
            return
        }
        this.items = items
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_chat_dialog, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindHolder(holder: ItemViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        // Process here
        if (item.type == QBDialogType.GROUP.code) {
            if (item.photo != null) {
                GlideHelper.displayAvatar(holder.itemBinding.imvAvatar, item.photo)
            } else {
                holder.itemBinding.imvAvatar.setImageResource(R.drawable.group_default)
            }
        } else {
            GlideHelper.displayAvatar(holder.itemBinding.imvAvatar, item.photo)
        }

        holder.itemBinding.tvName.text = getDialogName(item)
        holder.itemBinding.tvLastMessage.text = prepareTextLastMessage(item)

        val unreadMessagesCount = getUnreadMsgCount(item)
        if (unreadMessagesCount == 0) {
            holder.itemBinding.tvUnreadCount.setVisibility(View.GONE)
        } else {
            holder.itemBinding.tvUnreadCount.setVisibility(View.VISIBLE)
            holder.itemBinding.tvUnreadCount.setText((if (unreadMessagesCount > 99) 99 else unreadMessagesCount).toString())
        }
        holder?.itemBinding?.cardView?.setOnClickListener({
            listener?.onItemClick(item, position)
        })
        (holder?.itemBinding?.tvTime as RelativeTimeTextView).setReferenceTime(item.lastMessageDate!!)
        holder?.itemBinding?.executePendingBindings()
    }

    private fun getUnreadMsgCount(chatDialog: ChatDialog): Int {
        val unreadMessageCount = chatDialog.unreadMessagesCount
        return unreadMessageCount ?: 0
    }

    private fun isLastMessageAttachment(dialog: ChatDialog): Boolean {
        val lastMessage = dialog.lastMessageText
        val lastMessageSenderId = dialog.lastMessageSenderId
        return TextUtils.isEmpty(lastMessage) && lastMessageSenderId != null
    }

    private fun prepareTextLastMessage(chatDialog: ChatDialog): String {
        return if (isLastMessageAttachment(chatDialog)) {
            if (chatDialog.lastMessageContentType == ChatMessageContentType.LOCATION.type) {
                ctx.getString(R.string.chat_location)
            } else if (chatDialog.lastMessageContentType == ChatMessageContentType.AUDIO.type) {
                ctx.getString(R.string.chat_audio)
            } else if (chatDialog.lastMessageContentType == ChatMessageContentType.VIDEO.type) {
                ctx.getString(R.string.chat_video)
            } else if (chatDialog.lastMessageContentType == ChatMessageContentType.IMAGE.type) {
                ctx.getString(R.string.chat_image)
            } else if (chatDialog.lastMessageContentType == ChatMessageContentType.CREDIT.type) {
                ctx.getString(R.string.chat_credit)
            } else if (chatDialog.lastMessageContentType == ChatMessageContentType.GIFT.type) {
                ctx.getString(R.string.chat_gift)
            } else {
                ctx.getString(R.string.chat_attachment)
            }
        } else {
            if (chatDialog.lastMessageText != null) chatDialog.lastMessageText!! else ""
        }
    }

    private fun getDialogName(dialog: ChatDialog): String {
        // TODO: need to query from local DB
        if (dialog.type == QBDialogType.GROUP.code) {
            return dialog.name!!
        } else {
            // It's a private dialog, let's use opponent's name as chat name
        }
        return dialog.name!!
    }


    override fun onBindLoadingView(loadingText: TextView) {
        loadingText.setText("Loading...")
    }

    override fun onBindErrorView(errorText: TextView) {
        errorText.setText("Reload")
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = ItemChatDialogBinding.bind(itemView)
    }

    open interface OnItemClickListener {

        fun onItemClick(item: ChatDialog, position: Int)

    }

}