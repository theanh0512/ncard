package com.user.ncard.ui.chats.groups

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.ItemChatGroupMemberListBinding
import com.user.ncard.ui.catalogue.utils.DefaultEndlessRecyclerAdapter
import com.user.ncard.vo.Friend

/**
 * Created by Concaro on 7/17/2017.
 */
class ChatGroupMemberListAdapter(val ctx: Context,
                                 var items: List<Friend>,
                                 var allFriends: List<Friend>?,
                                 var currentUserLoginChatId: Int?,
                                 val OwnerGroupChatId: Int?) : DefaultEndlessRecyclerAdapter<ChatGroupMemberListAdapter.ItemViewHolder>() {


    fun updateItems(items: List<Friend>, allFriends: List<Friend>?) {
        if (items == null) {
            return
        }
        this.allFriends = allFriends
        this.items = items
    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_chat_group_member_list, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindHolder(holder: ItemViewHolder?, position: Int) {
        val holder = holder as ItemViewHolder
        val item = items.get(position)
        holder?.itemBinding?.item = item
        // Process here

        if (isFriend(item)) {
            holder?.itemBinding?.tvName?.text = ctx.getString(R.string.display_name, item.firstName, item.lastName)
        } else {
            holder?.itemBinding?.tvName?.text = ctx.getString(R.string.display_name, item.firstName, item.lastName) + "(not friend)"
        }

        if (currentUserLoginChatId == item.chatId) {
            holder?.itemBinding?.tvName?.text = "You"
        }
        if (OwnerGroupChatId == item.chatId) {
            // This is admin
            holder?.itemBinding?.tvAdmin?.visibility = View.VISIBLE
        } else {
            holder?.itemBinding?.tvAdmin?.visibility = View.GONE
        }

        holder?.itemBinding?.executePendingBindings()
    }

    fun isFriend(friend: Friend): Boolean {
        allFriends?.forEach {
            if (it?.id == friend.id) {
                return true
            }
        }
        return false
    }

    override fun onBindLoadingView(loadingText: TextView) {
        loadingText.setText(R.string.loading)
    }

    override fun onBindErrorView(errorText: TextView) {
        errorText.setText(R.string.reload)
    }

    override fun getCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemBinding = ItemChatGroupMemberListBinding.bind(itemView)
    }

}