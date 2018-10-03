package com.user.ncard.ui.discovery

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemFriendRequestSentBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.User

/**
 * Created by Pham on 11/8/17.
 */

class FriendRequestSentAdapter(private val userClickCallback: UserClickCallback?) :
        DataBoundListAdapter<User, ListItemFriendRequestSentBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemFriendRequestSentBinding {
        val binding = DataBindingUtil.inflate<ListItemFriendRequestSentBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_friend_request_sent, parent, false)!!
        binding.root.setOnClickListener {
            val user = binding.user
            if (user != null && userClickCallback != null) {
                userClickCallback.onClick(user)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemFriendRequestSentBinding, item: User) {
        binding.user = item
    }

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id && oldItem.status.equals(newItem.status)
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id && oldItem.status.equals(newItem.status)
    }

    interface UserClickCallback {
        fun onClick(user: User)
    }
}