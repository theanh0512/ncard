package com.user.ncard.ui.card.namecard

import android.databinding.DataBindingUtil
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemImageFromUriBinding
import com.user.ncard.ui.common.DataBoundListAdapter

/**
 * Created by Pham on 11/8/17.
 */

class ImageFromUriAdapter(private val onClickClickCallback: OnClickCallback?) : DataBoundListAdapter<Any,
        ListItemImageFromUriBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemImageFromUriBinding {
        val binding = DataBindingUtil.inflate<ListItemImageFromUriBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_image_from_uri, parent, false)!!
        binding.imageViewBackgroundItem.setOnClickListener {
            val source = binding.source
            if (source != null && onClickClickCallback != null) {
                onClickClickCallback.onClick(source)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemImageFromUriBinding, item: Any) {
        binding.source = item
    }

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallback {
        fun onClick(source: Any)
    }
}