package com.user.ncard.ui.me.gift

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemShopItemBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.GiftItem

/**
 * Created by Pham on 11/8/17.
 */

class ItemAdapter(private val onClickCallback: OnClickCallback?, private val onClickBuyCallback: onBuyClickCallBack?) : DataBoundListAdapter<GiftItem,
        ListItemShopItemBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemShopItemBinding {
        val binding = DataBindingUtil.inflate<ListItemShopItemBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_shop_item, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.root.setOnClickListener {
            val giftItem = binding.giftItem
            onClickCallback?.onClick(giftItem)
        }
        binding.buttonBuy.setOnClickListener {
            val giftItem = binding.giftItem
            onClickBuyCallback?.onClick(giftItem)
        }
        return binding
    }

    override fun bind(binding: ListItemShopItemBinding, item: GiftItem) {
        binding.giftItem = item
    }

    override fun areItemsTheSame(oldItem: GiftItem, newItem: GiftItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: GiftItem, newItem: GiftItem): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallback {
        fun onClick(item: GiftItem)
    }

    interface onBuyClickCallBack {
        fun onClick(item: GiftItem)
    }
}