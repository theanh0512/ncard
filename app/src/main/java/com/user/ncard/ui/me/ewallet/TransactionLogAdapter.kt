package com.user.ncard.ui.me.ewallet

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.ListItemTransactionLogBinding
import com.user.ncard.ui.common.DataBoundListAdapter
import com.user.ncard.vo.TransactionLog

/**
 * Created by Pham on 11/8/17.
 */

class TransactionLogAdapter(private val onClickCallback: OnClickCallback?, private val callbacks: LoaderCallbacks?) : DataBoundListAdapter<TransactionLog,
        ListItemTransactionLogBinding>() {
    private var isLoading: Boolean = false
    private var pastVisibleItems: Int = 0

    private fun isLoading(): Boolean {
        return isLoading
    }

    fun setLoadingOffset(loadingOffset: Int) {
        this.loadingOffset = loadingOffset
    }

    private var loadingOffset = 0

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            loadNextItemsIfNeeded(recyclerView)
        }
    }

    private fun loadNextItemsIfNeeded(recyclerView: RecyclerView?) {
        if (!isLoading) {
            val lastVisibleChild = recyclerView?.getChildAt(recyclerView.childCount - 1)
            val lastVisiblePos = recyclerView?.getChildAdapterPosition(lastVisibleChild)
            val total = itemCount

            if (lastVisiblePos != null && lastVisiblePos >= total - loadingOffset) {
                // We need to use runnable, since recycler view does not like when we are notifying
                // about changes during scroll callback.
                recyclerView.post(Runnable { loadNextItems() })
            }
        }
    }

    private fun loadNextItems() {
        if (!isLoading && callbacks != null && callbacks.canLoadNextItems()) {
            isLoading = true
            onLoadingStateChanged()
            callbacks.loadNextItems()
        }
    }

    private fun onLoadingStateChanged() {
        // No-default-op
    }

    fun onNextItemsLoaded() {
        if (isLoading) {
            isLoading = false
            onLoadingStateChanged()
        }
    }

    override fun createBinding(parent: ViewGroup): ListItemTransactionLogBinding {
        val binding = DataBindingUtil.inflate<ListItemTransactionLogBinding>(LayoutInflater.from(parent.context),
                R.layout.list_item_transaction_log, parent, false)!!
        //Todo: binding.getRoot().setOnClickListener and implement call back interface
        binding.root.setOnClickListener {
            val transactionLog = binding.transaction
            onClickCallback?.onClick(transactionLog)
        }
        return binding
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView?.addOnScrollListener(scrollListener)
        loadNextItemsIfNeeded(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView?.removeOnScrollListener(scrollListener)
    }

    override fun bind(binding: ListItemTransactionLogBinding, item: TransactionLog) {
        binding.transaction = item
    }

    override fun areItemsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
        return oldItem == newItem
    }

    interface OnClickCallback {
        fun onClick(item: TransactionLog)
    }

    interface LoaderCallbacks {
        fun canLoadNextItems(): Boolean

        fun loadNextItems()
    }
}