package com.user.ncard.ui.me.ewallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.user.ncard.R
import com.user.ncard.databinding.FragmentWithdrawBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.Utils
import javax.inject.Inject

class WithdrawFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentWithdrawBinding
    var walletPassword: String? = null
    var withdrawSuccessAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_withdraw, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.withdraw)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        viewDataBinding.viewModel = viewModel

        viewModel.start.value = true
        viewModel.walletInfo.observe(this@WithdrawFragment, Observer { response ->
            if (response != null) {
                walletPassword = response.walletPassword
            }
        })


        viewDataBinding.buttonConfirm.setOnClickListener {
            try {
                if (viewModel.ewalletSettingResponse != null && viewModel.ewalletSettingResponse?.value != null
                        && viewModel.amount.get().toString().toDouble() >= viewModel.ewalletSettingResponse?.value?.minWithdrawAmount!!
                        && viewModel.amount.get().toString().toDouble() <= viewModel.ewalletSettingResponse?.value?.maxWithdrawAmount!!) {
                    viewDataBinding.progressBar3.visibility = View.VISIBLE
                    viewModel.withdrawCash(walletPassword)
                } else {
                    Functions.showToastShortMessage(activity,
                            "Please input your withdraw money between ${viewModel.ewalletSettingResponse?.value?.minWithdrawAmount?.toInt()} and ${viewModel.ewalletSettingResponse?.value?.maxWithdrawAmount?.toInt()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModel.withdrawSuccess.observe(this@WithdrawFragment, Observer {
            viewDataBinding.progressBar3.visibility = View.GONE
            withdrawSuccessAlert = Utils.showAlert(activity, getString(R.string.money_withdraw_notice))
            object : CountDownTimer(1500.toLong(), 1000.toLong()) {
                override fun onFinish() {
                    withdrawSuccessAlert?.cancel()
                    activity.onBackPressed()
                }

                override fun onTick(millisUntilFinished: Long) {
                }

            }.start()
        })

        viewModel.errorResponse.observe(this, Observer { errorResponse ->
            if (errorResponse != null) {
                viewDataBinding.progressBar3.visibility = View.GONE
                withdrawSuccessAlert?.cancel()
                Functions.showSnackbarLongMessage(view, Functions.getMessageFromRetrofitException(errorResponse))
            }
        })

        viewModel.getEwalletSetting()
        viewModel.ewalletSettingResponse.observe(this, Observer {
            // Just get ewallet to compare
        })
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