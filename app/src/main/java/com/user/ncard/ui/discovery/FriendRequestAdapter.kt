package com.user.ncard.ui.discovery

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemFriendRequestBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.User

/**
 * Created by Pham on 11/8/17.
 */

class FriendRequestAdapter(private val rejectClickCallback: RejectClickCallback?,
                           private val acceptClickCallback: AcceptClickCallback?) :
        DataBoundListAdapter<User, ListItemFriendRequestBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemFriendRequestBinding {
        val binding = DataBindingUtil.inflate<ListItemFriendRequestBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_friend_request, parent, false)!!
        binding.imageViewReject.setOnClickListener {
            val user = binding.user
            if (user != null && rejectClickCallback != null) {
                rejectClickCallback.onClick(user)
            }
        }
        binding.imageViewAccept.setOnClickListener {
            val user = binding.user
            if (user != null && acceptClickCallback != null) {
                acceptClickCallback.onClick(user)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemFriendRequestBinding, item: User) {
        binding.user = item
    }

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id && oldItem.status.equals(newItem.status)
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id && oldItem.status.equals(newItem.status)
    }

    interface RejectClickCallback {
        fun onClick(user: User)
    }

    interface AcceptClickCallback {
        fun onClick(user: User)
    }
}