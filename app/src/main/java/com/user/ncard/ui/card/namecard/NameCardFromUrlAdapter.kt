package com.user.ncard.ui.card.namecard

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemImageFromUrlBinding
import com.user.ncard.ui.common.DataBoundListAdapter

/**
 * Created by Pham on 11/8/17.
 */

class NameCardFromUrlAdapter(private val onClickClickCallback: OnClickCallback?) : DataBoundListAdapter<String,
        ListItemImageFromUrlBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemImageFromUrlBinding {
        val binding = DataBindingUtil.inflate<ListItemImageFromUrlBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_image_from_url, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.imageViewBackgroundItem.setOnClickListener {
            val url = binding.url
            if (url != null && onClickClickCallback != null) {
                onClickClickCallback.onClick(url)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemImageFromUrlBinding, item: String) {
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