package com.user.ncard.ui.card

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemSearchUserBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.User

/**
 * Created by Pham on 11/8/17.
 */

class SearchUserAdapter(private val userClickCallback: UserClickCallback?,
                        private val addClickCallback: AddFriendCallback?) : DataBoundListAdapter<User,
        ListItemSearchUserBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemSearchUserBinding {
        val binding = DataBindingUtil.inflate<ListItemSearchUserBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_search_user, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.root.setOnClickListener {
            val user = binding.user
            if (user != null && userClickCallback != null) {
                userClickCallback.onClick(user)
            }
        }
        binding.imageViewAddFriend.setOnClickListener {
            val user = binding.user
            if (user != null && addClickCallback != null) {
                addClickCallback.onClick(user)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemSearchUserBinding, item: User) {
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

    interface AddFriendCallback {
        fun onClick(user: User)
    }
}
