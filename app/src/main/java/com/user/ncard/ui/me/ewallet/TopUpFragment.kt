package com.user.ncard.ui.me.ewallet

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.stripe.android.*
import com.stripe.android.model.*
import com.stripe.android.view.PaymentMethodsActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentTopUpBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.Config
import com.user.ncard.util.Utils
import com.user.ncard.vo.DepositTransaction
import okhttp3.ResponseBody
import retrofit2.HttpException
import javax.inject.Inject

class TopUpFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentTopUpBinding
    private lateinit var mPaymentSession: PaymentSession
    var processingAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_top_up, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.top_up)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)

        viewDataBinding.textViewCard.setOnClickListener {
            val payIntent = PaymentMethodsActivity.newIntent(activity)
            startActivityForResult(payIntent, REQUEST_CODE_SELECT_SOURCE)
//            mPaymentSession.presentPaymentMethodSelection()
        }

        viewDataBinding.buttonConfirm.apply {
            isEnabled = false
            setBackgroundResource(R.drawable.rounded_corner_button_disabled)
        }
        viewModel.start.value = true
        viewModel.response.observe(this@TopUpFragment, Observer<ResponseBody> { response ->
            if (response != null) {
                viewDataBinding.buttonConfirm.apply {
                    isEnabled = true
                    setBackgroundResource(R.drawable.rounded_corner_button)
                }
                PaymentConfiguration.init(Config.STRIPE_KEY)
                CustomerSession.initCustomerSession(NCardEphemeralKeyProvider(response.string()))
                setCardSource()
            }
        })

        setupPaymentSession()

        viewDataBinding.buttonConfirm.setOnClickListener {
            if (viewDataBinding.editTextTopUpAmount.text.isNullOrEmpty()) {
                Functions.showToastShortMessage(activity, "Please intput your money")
            } else {
                try {
                    if (viewModel.ewalletSettingResponse != null && viewModel.ewalletSettingResponse?.value != null
                            && viewDataBinding.editTextTopUpAmount.text.toString().toDouble() >= viewModel.ewalletSettingResponse?.value?.minTopupAmount!!
                            && viewDataBinding.editTextTopUpAmount.text.toString().toDouble() <= viewModel.ewalletSettingResponse?.value?.maxTopupAmount!!) {
                        if (processingAlert == null) processingAlert = Utils.showAlert(activity)
                        else processingAlert?.show()
                        if (viewDataBinding.editTextTopUpAmount.text.isNotEmpty()) attemptPurchase()
                    } else {
                        Functions.showToastShortMessage(activity,
                                "Please intput your money between ${viewModel.ewalletSettingResponse?.value?.minTopupAmount?.toInt()} and ${viewModel.ewalletSettingResponse?.value?.maxTopupAmount?.toInt()}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        viewModel.transactionResponse.observe(this@TopUpFragment, Observer { response ->
            if (response != null) {
                processingAlert?.cancel()
                activity.onBackPressed()
            }
        })

        viewModel.errorResponse.observe(this@TopUpFragment, Observer { errorResponse ->
            if (errorResponse != null) {
                processingAlert?.cancel()
                Functions.showSnackbarShortMessage(view, Functions.getMessageFromRetrofitException(errorResponse))
            }
        })

        viewModel.getEwalletSetting()
        viewModel.ewalletSettingResponse.observe(this, Observer {
            // Just get ewallet to compare
        })
    }

    private fun attemptPurchase() {
        CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
            override fun onCustomerRetrieved(customer: Customer) {
                val sourceId = customer.defaultSource
                if (sourceId == null) {
                    displayError("No payment method selected")
                    processingAlert?.cancel()
                    return
                }
                val source = customer.getSourceById(sourceId)
                proceedWithPurchaseIf3DSCheckIsNotNecessary(source, customer.id)
            }

            override fun onError(errorCode: Int, errorMessage: String?) {
                displayError("Error getting payment method")
            }
        })

    }

    private fun setupPaymentSession() {
        mPaymentSession = PaymentSession(activity)
        mPaymentSession.init(object : PaymentSession.PaymentSessionListener {
            override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
            }

            override fun onError(errorCode: Int, errorMessage: String?) {
                displayError(errorMessage)
            }

            override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
                if (data.selectedPaymentMethodId != null) {
                    CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
                        override fun onCustomerRetrieved(customer: Customer) {
                            val sourceId = customer.defaultSource
                            if (sourceId == null) {
                                displayError("No payment method selected")
                                return
                            }
                            val source = customer.getSourceById(sourceId)
                            val cardData = source?.asSource()?.sourceTypeModel as SourceCardData?
                            val card = source?.asCard()
                            if (cardData != null) {
                                viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, cardData?.last4)
                                setCardLogo(cardData.brand)
                            } else if (card != null) {
                                viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, card.last4)
                                setCardLogo(card.brand)
                            }
                        }

                        override fun onError(errorCode: Int, errorMessage: String?) {
                            displayError(errorMessage)
                        }
                    })
                }

            }
        }, PaymentSessionConfig.Builder().build())
    }

    private fun proceedWithPurchaseIf3DSCheckIsNotNecessary(customerSource: CustomerSource?, customerId: String) {
        val source = customerSource?.asSource()
        val card = customerSource?.asCard()
        if ((source == null || Source.CARD != source.type) && card == null) {
            displayError("Something went wrong - this should be rare")
            return
        }

        val cardData = source?.sourceTypeModel as SourceCardData?
        if (SourceCardData.REQUIRED == cardData?.threeDSecureStatus) {
            // In this case, you would need to ask the user to verify the purchase.
            // You can see an example of how to do this in the 3DS example application.
            // In stripe-android/example.
        } else {
            // If 3DS is not required, you can charge the source.
            //when source is null, that's mean the charge is from card
            //the source id will be the card id instead
            if (source != null) {
                viewModel.postDepositTransaction(DepositTransaction("stripe",
                        viewDataBinding.editTextTopUpAmount.text.toString().toInt(), source.currency?.capitalize() ?: "SGD", source.id))
            } else if (card != null) {
                viewModel.postDepositTransaction(DepositTransaction("stripe",
                        viewDataBinding.editTextTopUpAmount.text.toString().toInt(), source?.currency?.capitalize() ?: "SGD", customerSource.id ?: card.id))
            }
        }
    }

    private fun displayError(errorMessage: String?) {
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle("Error")
        alertDialog.setMessage(errorMessage)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    private fun setCardSource() {
        CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerSession.CustomerRetrievalListener {
            override fun onError(errorCode: Int, errorMessage: String?) {

            }

            override fun onCustomerRetrieved(customer: Customer) {
                val customerSource = customer.getSourceById(customer.defaultSource)
                when {
                    customerSource?.sourceType == "card" -> {
                        val cardData = customerSource.asSource()?.sourceTypeModel as SourceCardData?
                        val card = customerSource.asCard()
                        if (cardData != null) {
                            viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, cardData?.last4)
                            setCardLogo(cardData.brand)
                        } else if (card != null) {
                            viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, card.last4)
                            setCardLogo(card.brand)
                        }
                    }
                    customer.sources.size > 0 && customer.sources[0]?.sourceType == "card" -> {
                        val cardData = customer.sources[0].asSource()?.sourceTypeModel as SourceCardData?
                        val card = customer.sources[0].asCard()
                        if (cardData != null) {
                            viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, cardData.last4)
                            setCardLogo(cardData.brand)
                        } else if (card != null) {
                            viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, card.last4)
                            setCardLogo(card.brand)
                        }
                    }
                    else -> viewDataBinding.textViewCard.text = getString(R.string.add_new_card)
                }
            }
        })
    }

    private fun setCardLogo(brand: String?) {
        when (brand) {
            Card.AMERICAN_EXPRESS -> viewDataBinding.imageViewCardLogo.setImageResource(R.drawable.ic_amex)
            Card.VISA -> viewDataBinding.imageViewCardLogo.setImageResource(R.drawable.ic_visa)
            Card.MASTERCARD -> viewDataBinding.imageViewCardLogo.setImageResource(R.drawable.ic_mastercard)
            else -> viewDataBinding.imageViewCardLogo.setImageResource(R.drawable.ic_unknown)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_SOURCE && resultCode == RESULT_OK) {
            val selectedSource = data?.getStringExtra(PaymentMethodsActivity.EXTRA_SELECTED_PAYMENT)
            val source = Source.fromString(selectedSource)
            val card = Card.fromString(selectedSource)
            // Note: it isn't possible for a null or non-card source to be returned.
            if (source != null && Source.CARD == source.type) {
                val cardData = source.sourceTypeModel as SourceCardData
                viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, cardData.last4)
                setCardLogo(cardData.brand)
            } else if (card != null) {
                viewDataBinding.textViewCard.text = getString(R.string.display_last_4_digit, card.last4)
                setCardLogo(card.brand)
            }
        }
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