package com.user.ncard.ui.card

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemFriendBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Job

/**
 * Created by Pham on 11/8/17.
 */

class FriendAdapter(private val friendClickCallback: FriendClickCallback?) :
        DataBoundListAdapter<Friend, ListItemFriendBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemFriendBinding {
        val binding = DataBindingUtil.inflate<ListItemFriendBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_friend, parent, false)!!
        binding.root.setOnClickListener {
            val user = binding.user
            if (user != null && friendClickCallback != null) {
                friendClickCallback.onClick(user)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemFriendBinding, item: Friend) {
        binding.user = item
        if (getLastCompanyInListJob(item) != null && getLastCompanyInListJob(item)?.companyName != null) {
            binding.textViewJobStatus.text = getLastCompanyInListJob(item)?.companyName
            binding.textViewJobStatus.visibility = View.VISIBLE
        } else {
            binding.textViewJobStatus.visibility = View.GONE
        }
    }

    fun getLastCompanyInListJob(item: Friend): Job? {
        if (item != null && item.jobs != null && item?.jobs?.isNotEmpty()!!) {
            return item.jobs?.last()
        }
        return null
    }

    override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem.id == newItem.id && oldItem.remark.equals(newItem.remark)
    }

    override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
        return oldItem.id == newItem.id && oldItem.remark.equals(newItem.remark)
    }

    interface FriendClickCallback {
        fun onClick(user: Friend)
    }
}