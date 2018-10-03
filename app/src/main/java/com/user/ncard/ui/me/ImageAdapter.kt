package com.user.ncard.ui.me

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemImageSquareBinding
import com.user.ncard.ui.common.DataBoundListAdapter

/**
 * Created by Pham on 11/8/17.
 */

class ImageAdapter(private val onClickClickCallback: OnClickCallback?) : DataBoundListAdapter<String,
        ListItemImageSquareBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemImageSquareBinding {
        val binding = DataBindingUtil.inflate<ListItemImageSquareBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_image_square, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.imageView.setOnClickListener {
            val url = binding.url
            if (url != null && onClickClickCallback != null) {
                onClickClickCallback.onClick(url)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemImageSquareBinding, item: String) {
        binding.url = item
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