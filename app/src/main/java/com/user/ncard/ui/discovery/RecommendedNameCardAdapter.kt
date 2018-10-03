package com.user.ncard.ui.discovery

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemRecommendedNameCardBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.NameCardRecommendation

/**
 * Created by Pham on 11/8/17.
 */

class RecommendedNameCardAdapter(private val userClickCallback: UserClickCallback?,
                                 private val addClickCallback: AddNameCardCallback?) : DataBoundListAdapter<NameCardRecommendation,
        ListItemRecommendedNameCardBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemRecommendedNameCardBinding {
        val binding = DataBindingUtil.inflate<ListItemRecommendedNameCardBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_recommended_name_card, parent, false)!!
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

    override fun bind(binding: ListItemRecommendedNameCardBinding, item: NameCardRecommendation) {
        binding.recommendation = item
    }

    override fun areItemsTheSame(oldItem: NameCardRecommendation, newItem: NameCardRecommendation): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NameCardRecommendation, newItem: NameCardRecommendation): Boolean {
        return oldItem.suggestedNameCard.id == newItem.suggestedNameCard.id
    }

    interface UserClickCallback {
        fun onClick(friendRecommendation: NameCardRecommendation)
    }

    interface AddNameCardCallback {
        fun onClick(friendRecommendation: NameCardRecommendation)
    }
}
