package com.user.ncard.ui.me.gift

import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemDisplayCategoryBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.DisplayCategory
import com.user.ncard.vo.GiftItem

/**
 * Created by Pham on 11/8/17.
 */

class DisplayCategoryAdapter(private val onClickCallBack: OnClickCallBack?, private val onItemBuyClickCallBack: OnItemBuyClickCallBack,
                             private val onItemClickCallBack: OnItemClickCallBack) :
        DataBoundListAdapter<DisplayCategory, ListItemDisplayCategoryBinding>() {
    override fun createBinding(parent: ViewGroup): ListItemDisplayCategoryBinding {
        val binding = DataBindingUtil.inflate<ListItemDisplayCategoryBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_display_category, parent, false)!!
        binding.root.setOnClickListener {
            val group = binding.displayCategory
            if (group != null && onClickCallBack != null) {
                onClickCallBack.onClick(group)
            }
        }

        return binding
    }

    override fun bind(binding: ListItemDisplayCategoryBinding, item: DisplayCategory) {
        binding.displayCategory = item
        val itemAdapter = ItemAdapter(object : ItemAdapter.OnClickCallback {
            override fun onClick(item: GiftItem) {
                onItemClickCallBack.onClick(item)
            }
        }, object : ItemAdapter.onBuyClickCallBack {
            override fun onClick(item: GiftItem) {
                onItemBuyClickCallBack.onClick(item)
            }

        })
        binding.recyclerViewItem.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
        val itemList = binding.displayCategory?.itemList
        itemAdapter.replace(itemList)
        itemAdapter.notifyDataSetChanged()
    }

    override fun areItemsTheSame(oldItem: DisplayCategory, newItem: DisplayCategory): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DisplayCategory, newItem: DisplayCategory): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallBack {
        fun onClick(displayCategory: DisplayCategory)
    }

    interface OnItemBuyClickCallBack {
        fun onClick(giftItem: GiftItem)
    }

    interface OnItemClickCallBack {
        fun onClick(giftItem: GiftItem)
    }
}