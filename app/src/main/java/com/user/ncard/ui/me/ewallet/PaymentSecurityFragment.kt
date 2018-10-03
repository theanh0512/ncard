package com.user.ncard.ui.me.ewallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.TextView
import com.bigbangbutton.editcodeview.EditCodeView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentPaymentSecurityBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import javax.inject.Inject

class PaymentSecurityFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentPaymentSecurityBinding
    lateinit var inputPasswordAlert: AlertDialog
    lateinit var wrongPasswordAlert: AlertDialog
    var currentPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_payment_security, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.payment_security)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        viewDataBinding.apply {
            textViewCreatePassword.setOnClickListener {
                val setPaymentPasswordFragment = SetPaymentPasswordFragment.newInstance(false, "")
                fragmentManager.beginTransaction()
                        .replace(R.id.container, setPaymentPasswordFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
            textViewChangePaymentPassword.setOnClickListener {
                showDialogMessage()
            }
            textViewForgetPaymentPassword.setOnClickListener {
                val forgetPasswordFragment = ForgetPasswordFragment()
                fragmentManager.beginTransaction()
                        .replace(R.id.container, forgetPasswordFragment)
                        .addToBackStack("PaymentSecurityFragment")
                        .commitAllowingStateLoss()
            }
        }
        viewModel.walletInfo.observe(this@PaymentSecurityFragment, Observer {
            if (it?.walletPassword == null) {
                viewDataBinding.apply {
                    textViewCreatePassword.visibility = View.VISIBLE
                    textViewChangePaymentPassword.visibility = View.GONE
                    textViewForgetPaymentPassword.visibility = View.GONE
                }
            } else {
                currentPassword = it.walletPassword
                viewDataBinding.apply {
                    textViewCreatePassword.visibility = View.GONE
                    textViewChangePaymentPassword.visibility = View.VISIBLE
                    textViewForgetPaymentPassword.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun showDialogMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        val v: View = inflater.inflate(R.layout.dialog_change_password, null)
        builder.setView(v)
        val editCodeView = v.findViewById<EditCodeView>(R.id.editTextPassword)
        editCodeView.setEditCodeListener { code ->
            if (currentPassword != null) {
                if (code == currentPassword) {
                    inputPasswordAlert.cancel()
                    val setPaymentPasswordFragment = SetPaymentPasswordFragment.newInstance(true, currentPassword!!)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, setPaymentPasswordFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                } else {
                    inputPasswordAlert.cancel()
                    wrongPasswordAlert = Utils.showAlert(activity, getString(R.string.wrong_password))
                    object : CountDownTimer(2000.toLong(), 1000.toLong()) {
                        override fun onFinish() {
                            wrongPasswordAlert.cancel()
                        }

                        override fun onTick(millisUntilFinished: Long) {
                        }

                    }.start()
                }
            }
        }

        val forgetPasswordTextView = v.findViewById<TextView>(R.id.textViewForgetPassword)
        forgetPasswordTextView.setOnClickListener {
            inputPasswordAlert.cancel()
            val forgetPasswordFragment = ForgetPasswordFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, forgetPasswordFragment)
                    .addToBackStack("PaymentSecurityFragment")
                    .commitAllowingStateLoss()
        }
        inputPasswordAlert = builder.create()
        val window: Window = inputPasswordAlert.window
        val attributes = window.attributes
        attributes.gravity = Gravity.CENTER
        //attributes.y = 124
        attributes.flags = attributes.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = attributes
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        inputPasswordAlert.show()

    }

    override fun onStart() {
        super.onStart()
        viewModel.start.value = true
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