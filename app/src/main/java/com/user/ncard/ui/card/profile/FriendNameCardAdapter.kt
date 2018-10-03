package com.user.ncard.ui.card.profile

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemFriendNameCardBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.NameCard

/**
 * Created by Pham on 11/8/17.
 */

class FriendNameCardAdapter(private val nameCardClickCallback: NameCardClickCallback?) :
        DataBoundListAdapter<NameCard, ListItemFriendNameCardBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemFriendNameCardBinding {
        val binding = DataBindingUtil.inflate<ListItemFriendNameCardBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_friend_name_card, parent, false)!!
        binding.root.setOnClickListener {
            val nameCard = binding.user
            if (nameCard != null && nameCardClickCallback != null) {
                nameCardClickCallback.onClick(nameCard)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemFriendNameCardBinding, item: NameCard) {
        binding.user = item
    }

    override fun areItemsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem.id == newItem.id
    }

    interface NameCardClickCallback {
        fun onClick(nameCard: NameCard)
    }
}