package com.user.ncard.ui.me.gift

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemMyGiftBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.MyGiftData

/**
 * Created by Pham on 11/8/17.
 */

class GiftAdapter(private val onClickCallback: OnClickCallback?,
                  private val onClickSendCallback: OnClickSendCallBack?) : DataBoundListAdapter<MyGiftData,
        ListItemMyGiftBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemMyGiftBinding {
        val binding = DataBindingUtil.inflate<ListItemMyGiftBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_my_gift, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.root.setOnClickListener {
            val myGiftData = binding.gift
            onClickCallback?.onClick(myGiftData)
        }
        binding.buttonSend.setOnClickListener {
            val myGiftData = binding.gift
            onClickSendCallback?.onClick(myGiftData)
        }

        return binding
    }

    override fun bind(binding: ListItemMyGiftBinding, item: MyGiftData) {
        binding.gift = item
    }

    override fun areItemsTheSame(oldItem: MyGiftData, newItem: MyGiftData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MyGiftData, newItem: MyGiftData): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallback {
        fun onClick(item: MyGiftData)
    }

    interface OnClickSendCallBack {
        fun onClick(item: MyGiftData)
    }
}