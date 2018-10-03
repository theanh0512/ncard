package com.user.ncard.ui.group

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemGroupBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.Group

/**
 * Created by Pham on 11/8/17.
 */

class GroupAdapter(private val onClickCallBack: OnClickCallBack?) :
        DataBoundListAdapter<Group, ListItemGroupBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemGroupBinding {
        val binding = DataBindingUtil.inflate<ListItemGroupBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_group, parent, false)!!
        binding.root.setOnClickListener {
            val group = binding.group
            if (group != null && onClickCallBack != null) {
                onClickCallBack.onClick(group)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemGroupBinding, item: Group) {
        binding.group = item
    }

    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem.members == newItem.members && oldItem.name == newItem.name && oldItem.nameCards == newItem.nameCards
    }

    interface OnClickCallBack {
        fun onClick(group: Group)
    }
}