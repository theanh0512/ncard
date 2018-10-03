package com.user.ncard.ui.filter

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemFilterBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.FilterObject

/**
 * Created by Pham on 11/8/17.
 */

class FilterAdapter(private val filterOnClickCallback: FilterOnClickCallback?) :
        DataBoundListAdapter<FilterObject, ListItemFilterBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemFilterBinding {
        val binding = DataBindingUtil.inflate<ListItemFilterBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_filter, parent, false)!!
        binding.root.setOnClickListener {
            val filterObject = binding.filterObject
            if (filterObject != null && filterOnClickCallback != null) {
                filterOnClickCallback.onClick(filterObject)
                binding.imageViewCheck.apply {
                    visibility = if (visibility == View.GONE) View.VISIBLE
                    else View.GONE
                }
            }
        }
        return binding
    }

    override fun bind(binding: ListItemFilterBinding, item: FilterObject) {
        binding.filterObject = item
    }

    override fun areItemsTheSame(oldItem: FilterObject, newItem: FilterObject): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: FilterObject, newItem: FilterObject): Boolean {
        return oldItem.name == newItem.name
    }

    interface FilterOnClickCallback {
        fun onClick(filterObject: FilterObject)
    }
}