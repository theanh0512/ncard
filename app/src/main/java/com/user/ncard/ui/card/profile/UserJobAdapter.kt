package com.user.ncard.ui.card.profile

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemUserJobBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.Job

/**
 * Created by Pham on 11/8/17.
 */

class UserJobAdapter(private val onClickCallBack: OnClickCallBack?) :
        DataBoundListAdapter<Job, ListItemUserJobBinding>() {

    override fun createBinding(parent: ViewGroup): ListItemUserJobBinding {
        val binding = DataBindingUtil.inflate<ListItemUserJobBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_user_job, parent, false)!!
        binding.imageViewNameCard.setOnClickListener {
            val job = binding.job
            if (job != null && onClickCallBack != null) {
                onClickCallBack.onClick(job)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemUserJobBinding, item: Job) {
        binding.job = item
    }

    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallBack {
        fun onClick(job: Job)
    }
}