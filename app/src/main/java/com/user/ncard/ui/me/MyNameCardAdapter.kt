package com.user.ncard.ui.me

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemMyNameCardBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.NameCard

/**
 * Created by Pham on 11/8/17.
 */

class MyNameCardAdapter(private val onClickCallBack: OnClickCallBack?) :
        DataBoundListAdapter<NameCard, ListItemMyNameCardBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemMyNameCardBinding {
        val binding = DataBindingUtil.inflate<ListItemMyNameCardBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_my_name_card, parent, false)!!
        binding.root.setOnClickListener {
            val nameCard = binding.namecard
            if (nameCard != null && onClickCallBack != null) {
                onClickCallBack.onClick(nameCard)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemMyNameCardBinding, item: NameCard) {
        binding.namecard = item
    }

    override fun areItemsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NameCard, newItem: NameCard): Boolean {
        return oldItem.name == newItem.name && oldItem.company == newItem.company
    }

    interface OnClickCallBack {
        fun onClick(nameCard: NameCard)
    }
}