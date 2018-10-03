package com.user.ncard.ui.me.ewallet

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.MutableBoolean
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.stripe.android.CustomerSession
import com.stripe.android.PaymentConfiguration
import com.stripe.android.model.Customer
import com.stripe.android.view.PaymentMethodsActivity
import com.user.ncard.R
import com.user.ncard.api.NCardService
import com.user.ncard.databinding.FragmentEWalletBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.Config
import com.user.ncard.util.Utils
import okhttp3.ResponseBody
import java.math.BigDecimal
import javax.inject.Inject

class EWalletFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var nCardService: NCardService

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentEWalletBinding
    var currentPassword: String? = null
    val isLoading = MutableBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_e_wallet, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.e_wallet_title)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        viewDataBinding.imageViewPaymentMethod.isEnabled = false
        viewModel.walletInfo.observe(this@EWalletFragment, Observer {
            isLoading.value = false
            viewDataBinding.textViewBalance.text = getString(R.string.display_balance, it?.balance?.amount?.toDouble()?.roundTo2DecimalPlaces().toString())
            if (it != null) currentPassword = it.walletPassword
        })
        viewModel.response.observe(this@EWalletFragment, Observer<ResponseBody> { response ->
            if (response != null) {
                PaymentConfiguration.init(Config.STRIPE_KEY)
                CustomerSession.initCustomerSession(NCardEphemeralKeyProvider(response.string()))
                CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
                    override fun onCustomerRetrieved(customer: Customer) {
                        viewDataBinding.imageViewPaymentMethod.isEnabled = true
                    }

                    override fun onError(errorCode: Int, errorMessage: String?) {

                    }
                })
            }
        })
        viewDataBinding.imageViewPaymentMethod.setOnClickListener {
            val payIntent = PaymentMethodsActivity.newIntent(activity)
            startActivityForResult(payIntent, REQUEST_CODE_SELECT_SOURCE)
        }
        viewDataBinding.imageViewTopUp.setOnClickListener {
            val intent = Intent(activity, TopUpActivity::class.java)
            startActivity(intent)
        }
        viewDataBinding.imageViewPaymentSecurity.setOnClickListener {
            val paymentSecurityFragment = PaymentSecurityFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, paymentSecurityFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
        }
        viewDataBinding.imageViewTransfer.setOnClickListener {
            if (currentPassword != null) {
                val intent = Intent(activity, SelectFriendActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_SELECT_FRIEND)
            } else if (currentPassword == null && !isLoading.value) {
                Functions.showAlertDialog(activity, "", getString(R.string.warn_credit_transfer_no_pass))
            }
        }
        viewDataBinding.imageViewTransaction.setOnClickListener {
            val transactionLogFragment = TransactionLogFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, transactionLogFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
        }
        viewDataBinding.imageViewWithdraw.setOnClickListener {
            val withdrawFragment = WithdrawFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, withdrawFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_FRIEND) {
                if (data != null && currentPassword != null) {
                    val cashTransferFragment = CashTransferFragment.newInstance(data.getParcelableExtra("friend"), currentPassword!!)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, cashTransferFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        viewModel.start.value = true
        isLoading.value = true
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
        private const val REQUEST_CODE_SELECT_SOURCE = 55
        private const val REQUEST_CODE_SELECT_FRIEND = 56
    }
}

fun Double.roundTo2DecimalPlaces() = BigDecimal(this).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
