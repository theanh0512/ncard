package com.user.ncard.ui.me

import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemJobBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.Job

/**
 * Created by Pham on 11/8/17.
 */

class MyJobAdapter(private val onClickCallBack: OnClickCallBack?, private val onNameCardClickCallBack: OnNameCardClickCallBack? = null) :
        DataBoundListAdapter<Job, ListItemJobBinding>() {
    override fun createBinding(parent: ViewGroup): ListItemJobBinding {
        val binding = DataBindingUtil.inflate<ListItemJobBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_job, parent, false)!!
        binding.root.setOnClickListener {
            val group = binding.job
            if (group != null && onClickCallBack != null) {
                onClickCallBack.onClick(group)
            }
        }
        binding.imageViewNameCard.setOnClickListener {
            val job = binding.job
            if (job != null && onNameCardClickCallBack != null) {
                onNameCardClickCallBack.onClick(job)
            }
        }
        return binding
    }

    override fun bind(binding: ListItemJobBinding, item: Job) {
        binding.job = item
        val certAdapter = ImageAdapter(object : ImageAdapter.OnClickCallback {
            override fun onClick(url: String) {
            }
        })
        binding.recyclerViewLogo.apply {
            adapter = certAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        }
        if (binding.job != null && binding.job.cert != null) {
            certAdapter.replace(binding.job.cert)
            certAdapter.notifyDataSetChanged()
        }
        val productServiceAdapter = ImageAdapter(object : ImageAdapter.OnClickCallback {
            override fun onClick(url: String) {
            }
        })
        binding.recyclerViewProductService.apply {
            adapter = productServiceAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
        }
        if (binding.job != null && binding.job.media != null) {
            productServiceAdapter.replace(binding.job.media)
            productServiceAdapter.notifyDataSetChanged()
        }
    }

    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.jobTitle == newItem.jobTitle && oldItem.companyName == newItem.companyName && oldItem.industry == newItem.industry
    }

    interface OnClickCallBack {
        fun onClick(job: Job)
    }

    interface OnNameCardClickCallBack {
        fun onClick(job: Job)
    }
}