package com.user.ncard.ui.discovery

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemRecommendedUserBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.FriendRecommendation

/**
 * Created by Pham on 11/8/17.
 */

class RecommendedUserAdapter(private val userClickCallback: UserClickCallback?,
                             private val addClickCallback: AddFriendCallback?) : DataBoundListAdapter<FriendRecommendation,
        ListItemRecommendedUserBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemRecommendedUserBinding {
        val binding = DataBindingUtil.inflate<ListItemRecommendedUserBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_recommended_user, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.root.setOnClickListener {
            val user = binding.recommendation
            if (user != null && userClickCallback != null) {
                userClickCallback.onClick(user)
            }
        }
        binding.imageViewAddFriend.setOnClickListener {
            val user = binding.recommendation
            if (user != null && addClickCallback != null) {
                addClickCallback.onClick(user)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemRecommendedUserBinding, item: FriendRecommendation) {
        binding.recommendation = item
    }

    override fun areItemsTheSame(oldItem: FriendRecommendation, newItem: FriendRecommendation): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FriendRecommendation, newItem: FriendRecommendation): Boolean {
        return oldItem.suggestedFriend.id == newItem.suggestedFriend.id
    }

    interface UserClickCallback {
        fun onClick(friendRecommendation: FriendRecommendation)
    }

    interface AddFriendCallback {
        fun onClick(friendRecommendation: FriendRecommendation)
    }
}
