package com.user.ncard.ui.me.ewallet

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSetPaymentPasswordBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import javax.inject.Inject

class SetPaymentPasswordFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentSetPaymentPasswordBinding
    var processingAlert: AlertDialog? = null
    var createdPasswordAlert: AlertDialog? = null
    var filledPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_set_payment_password, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.set_payment_password)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        viewDataBinding.editTextPassword.apply {
            requestFocus()
            setEditCodeListener { code ->
                when {
                    filledPassword == null -> {
                        filledPassword = code
                        viewDataBinding.textViewInputPassword.text = context.getString(R.string.reinput_payment_password)
                        clearCode()
                    }
                    filledPassword != code -> {
                        viewDataBinding.apply {
                            textViewWrongRetype.visibility = View.VISIBLE
                            textViewInputPassword.text = context.getString(R.string.reinput_payment_password)
                        }
                    }
                //correct code
                    else -> {
                        viewDataBinding.textViewWrongRetype.visibility = View.GONE
                        processingAlert = Utils.showAlert(activity)
                        if (arguments.getBoolean(ARGUMENT_IS_CHANGING_PASSWORD) &&
                                arguments.getString(ARGUMENT_OLD_PASSWORD) != "") {
                            viewModel.changePaymentPassword(arguments.getString(ARGUMENT_OLD_PASSWORD), code)
                        } else viewModel.createPaymentPassword(code)
                    }
                }
            }
            setEditCodeWatcher { code ->
                if (filledPassword != null && code.length < 6) viewDataBinding.textViewWrongRetype.visibility = View.GONE
            }
        }
        viewModel.createPaymentPasswordSuccess.observe(this@SetPaymentPasswordFragment, Observer {
            processingAlert?.cancel()
            createdPasswordAlert = showAlert(activity, getString(R.string.payment_password_created))
        })

        viewModel.changePasswordSuccess.observe(this@SetPaymentPasswordFragment, Observer {
            processingAlert?.cancel()
            createdPasswordAlert = showAlert(activity, getString(R.string.change_password_successfully))
        })
    }

    fun showAlert(activity: Activity, message: String): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.cancel()
            if (arguments.getBoolean(ARGUMENT_IS_CHANGING_PASSWORD) && arguments.getString(ARGUMENT_OLD_PASSWORD) == "") {
                fragmentManager.popBackStack("PaymentSecurityFragment", 1)
            } else activity.onBackPressed()
        }
        builder.setTitle(R.string.cardline)
        builder.setIcon(R.drawable.check_black)
        val dialog = builder.create()
        dialog.show()
        return dialog
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
        fun newInstance(isChangingPassword: Boolean, oldPassword: String) = SetPaymentPasswordFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARGUMENT_IS_CHANGING_PASSWORD, isChangingPassword)
                putString(ARGUMENT_OLD_PASSWORD, oldPassword)
            }
        }

        const val ARGUMENT_IS_CHANGING_PASSWORD = "IS_CHANGING"
        const val ARGUMENT_OLD_PASSWORD = "OLD_PASSWORD"
    }
}