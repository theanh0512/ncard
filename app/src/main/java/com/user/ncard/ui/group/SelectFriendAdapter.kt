package com.user.ncard.ui.group

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemChooseFriendBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.Friend

/**
 * Created by Pham on 11/8/17.
 */

class SelectFriendAdapter(private val onClickCallback: OnClickCallback?) :
        DataBoundListAdapter<Friend, ListItemChooseFriendBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemChooseFriendBinding {
        val binding = DataBindingUtil.inflate<ListItemChooseFriendBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_choose_friend, parent, false)!!
        binding.root.setOnClickListener {
            val friend = binding.user
            if (friend != null && onClickCallback != null) {
                onClickCallback.onClick(friend)
                binding.imageViewCheck.apply {
                    visibility = if (visibility == View.GONE) View.VISIBLE
                    else View.GONE
                }
            }
        }
        return binding
    }

    override fun bind(binding: ListItemChooseFriendBinding, item: Friend) {
        binding.user = item
    }

    override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem.id == newItem.id && oldItem.isChecked == newItem.isChecked
    }

    interface OnClickCallback {
        fun onClick(friend: Friend)
    }
}