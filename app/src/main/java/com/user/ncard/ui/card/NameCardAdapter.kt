package com.user.ncard.ui.card

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemNameCardBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.NameCard

/**
 * Created by Pham on 11/8/17.
 */

class NameCardAdapter(private val nameCardClickCallback: NameCardClickCallback?) :
        DataBoundListAdapter<NameCard, ListItemNameCardBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemNameCardBinding {
        val binding = DataBindingUtil.inflate<ListItemNameCardBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_name_card, parent, false)!!
        binding.root.setOnClickListener {
            val nameCard = binding.user
            if (nameCard != null && nameCardClickCallback != null) {
                nameCardClickCallback.onClick(nameCard)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemNameCardBinding, item: NameCard) {
        binding.user = item
    }

    override fun areItemsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem.id == newItem.id && oldItem.remark.equals(newItem.remark)
    }

    override fun areContentsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem.id == newItem.id && oldItem.remark.equals(newItem.remark)
    }

    interface NameCardClickCallback {
        fun onClick(nameCard: NameCard)
    }
}