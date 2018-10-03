package com.user.ncard.ui.card.namecard

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.user.ncard.R
import com.user.ncard.databinding.ListItemChooseBackgroundBinding
import com.user.ncard.ui.common.DataBoundListAdapter

/**
 * Created by Pham on 11/8/17.
 */

class NameCardBackgroundAdapter(private val onClickClickCallback: OnClickCallback?) : DataBoundListAdapter<String,
        ListItemChooseBackgroundBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemChooseBackgroundBinding {
        val binding = DataBindingUtil.inflate<ListItemChooseBackgroundBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_choose_background, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.imageViewBackgroundItem.setOnClickListener {
            val url = binding.url
            if (url != null && onClickClickCallback != null) {
                onClickClickCallback.onClick(url)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemChooseBackgroundBinding, item: String) {
        binding.url = item
//        Glide.with(binding.imageViewBackgroundItem.context).load(item).into(binding.imageViewBackgroundItem)
    }

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallback {
        fun onClick(url: String)
    }
}