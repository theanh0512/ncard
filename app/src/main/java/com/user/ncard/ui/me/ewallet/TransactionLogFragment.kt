package com.user.ncard.ui.me.ewallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentTransactionLogBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import com.user.ncard.vo.TransactionLog
import javax.inject.Inject

class TransactionLogFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentTransactionLogBinding
    lateinit var transactionLogAdapter: TransactionLogAdapter
    val transactionLogs = ArrayList<TransactionLog>()
    lateinit var processionAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_transaction_log, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.transaction)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        processionAlert = Utils.showAlert(activity)
        transactionLogAdapter = TransactionLogAdapter(object : TransactionLogAdapter.OnClickCallback {
            override fun onClick(item: TransactionLog) {
                val transactionLogDetailFragment = TransactionLogDetailFragment.newInstance(item.id)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, transactionLogDetailFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }

        }, object : TransactionLogAdapter.LoaderCallbacks {
            override fun canLoadNextItems(): Boolean {
                return viewModel.canLoadMore()
            }

            override fun loadNextItems() {
                return viewModel.loadMore()
            }
        })
        viewDataBinding.recyclerViewTransactionLog.apply {
            adapter = transactionLogAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        }
        transactionLogAdapter.setLoadingOffset(1)
        viewModel.transactionLogResponse.observe(this@TransactionLogFragment, Observer { transactionLogResponse ->
            if (transactionLogResponse?.data != null && transactionLogResponse.data.isNotEmpty()) {
                viewDataBinding.textViewNoTransaction.visibility = View.GONE
                if (processionAlert.isShowing) processionAlert.cancel()
                transactionLogAdapter.onNextItemsLoaded()
                viewModel.pagination = transactionLogResponse.pagination
                viewModel.page = transactionLogResponse.pagination.currentPage
                transactionLogs.addAll(transactionLogResponse.data)
                transactionLogAdapter.replace2(transactionLogs)
                transactionLogAdapter.notifyDataSetChanged()
            } else {
                if (processionAlert.isShowing) processionAlert.cancel()
                viewDataBinding.textViewNoTransaction.visibility = View.VISIBLE
            }
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.page = 1
        viewModel.pagination = null
        viewModel.startLoadTransactionLog.value = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val REQUEST_CODE_SELECT_SOURCE = 55
    }
}