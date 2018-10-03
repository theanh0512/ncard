package com.user.ncard.ui.group

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemChooseNameCardBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.NameCard

/**
 * Created by Pham on 11/8/17.
 */

class SelectNameCardAdapter(private val onClickCallback: OnClickCallback?) :
        DataBoundListAdapter<NameCard, ListItemChooseNameCardBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemChooseNameCardBinding {
        val binding = DataBindingUtil.inflate<ListItemChooseNameCardBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_choose_name_card, parent, false)!!
        binding.root.setOnClickListener {
            val nameCard = binding.user
            if (nameCard != null && onClickCallback != null) {
                onClickCallback.onClick(nameCard)
                binding.imageViewCheck.apply {
                    visibility = if (visibility == View.GONE) View.VISIBLE
                    else View.GONE
                }
            }
        }
        return binding
    }

    override fun bind(binding: ListItemChooseNameCardBinding, item: NameCard) {
        binding.user = item
    }

    override fun areItemsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem.id == newItem.id && oldItem.isChecked == newItem.isChecked
    }

    interface OnClickCallback {
        fun onClick(nameCard: NameCard)
    }
}