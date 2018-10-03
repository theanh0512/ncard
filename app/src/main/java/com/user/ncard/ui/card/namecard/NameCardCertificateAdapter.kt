package com.user.ncard.ui.card.namecard

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemCertificateSelectedBinding
import com.user.ncard.ui.common.DataBoundListAdapter

/**
 * Created by Pham on 11/8/17.
 */

class NameCardCertificateAdapter(private val onClickClickCallback: OnClickCallback?) : DataBoundListAdapter<Int,
        ListItemCertificateSelectedBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemCertificateSelectedBinding {
        val binding = DataBindingUtil.inflate<ListItemCertificateSelectedBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_certificate_selected, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.imageViewThumbnail.setOnClickListener {
            val url = binding.source
            if (url != null && onClickClickCallback != null) {
                onClickClickCallback.onClick(url as Int)
                binding.imageViewSelected.apply {
                    visibility = if (visibility == View.GONE) View.VISIBLE
                    else View.GONE
                }
            }
        }
        return binding
    }

    override fun bind(binding: ListItemCertificateSelectedBinding, item: Int) {
        binding.source = item
//        Glide.with(binding.imageViewBackgroundItem.context).load(item).into(binding.imageViewBackgroundItem)
    }

    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallback {
        fun onClick(url: Int)
    }
}