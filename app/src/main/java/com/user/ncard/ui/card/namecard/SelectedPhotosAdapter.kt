package com.user.ncard.ui.card.namecard

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemPhotoSelectedBinding
import com.user.ncard.ui.common.DataBoundListAdapter

/**
 * Created by Pham on 11/8/17.
 */

class SelectedPhotosAdapter(private val removeClickCallback: RemoveCallback?) : DataBoundListAdapter<Any,
        ListItemPhotoSelectedBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemPhotoSelectedBinding {
        val binding = DataBindingUtil.inflate<ListItemPhotoSelectedBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_photo_selected, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.imageViewRemove.setOnClickListener {
            val source = binding.source
            if (source != null && removeClickCallback != null) {
                removeClickCallback.onClick(source)
            }
        }
        binding.imageViewThumbnail.setOnClickListener {
            val uri = binding.source
            if (uri != null && removeClickCallback != null) {
                removeClickCallback.onClick(uri)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemPhotoSelectedBinding, item: Any) {
        binding.source = item
    }

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    interface RemoveCallback {
        fun onClick(source: Any)
    }
}