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
import android.text.Editable
import android.text.TextWatcher
import android.util.MutableBoolean
import android.view.*
import android.widget.TextView
import com.bigbangbutton.editcodeview.EditCodeView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCashTransferBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.TransferCreditEvent
import com.user.ncard.util.Utils
import com.user.ncard.vo.Friend
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class CashTransferFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentCashTransferBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: EWalletViewModel
    val friendsList = ArrayList<Friend>()
    lateinit var inputPasswordAlert: AlertDialog
    lateinit var wrongPasswordAlert: AlertDialog
    var currentPassword: String? = null
    var transferSuccessAlert: AlertDialog? = null
    val isCashSent = MutableBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_cash_transfer, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.cash_transfer, R.color.colorDarkBlue)
        currentPassword = arguments.getString(ARGUMENT_PASSWORD)
        viewDataBinding.editTextTransferAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!p0.isNullOrEmpty() && p0!!.toString().toFloat() >= 1.0f) {
                    viewDataBinding.buttonConfirm.apply {
                        setBackgroundResource(R.drawable.rounded_corner_button)
                        isEnabled = true
                    }
                } else {
                    viewDataBinding.buttonConfirm.apply {
                        setBackgroundResource(R.drawable.rounded_corner_button_disabled)
                        isEnabled = false
                    }
                }
            }
        })
        viewDataBinding.buttonConfirm.setOnClickListener {
            if (currentPassword != null) {
                showDialogMessage()
            } else {
                Functions.showAlertDialog(activity, "", getString(R.string.warn_credit_transfer_no_pass))
            }
        }

        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        viewDataBinding.friend = arguments.getParcelable(ARGUMENT_FRIEND)
        viewModel.transferCreditResponse.observe(this@CashTransferFragment, Observer {
            viewDataBinding.progressBar.visibility = View.GONE
            if (transferSuccessAlert == null) {
                transferSuccessAlert = Utils.showAlert(activity, getString(R.string.transfer_sucessfully))
            } else {
                if (transferSuccessAlert != null && !transferSuccessAlert!!.isShowing) {
                    transferSuccessAlert?.show()
                }
            }
            // Send chat message with credit information
            if (it != null && isCashSent.value) {
                //Functions.showLogMessage(TAG, it?.toString())
                EventBus.getDefault().post(TransferCreditEvent(it))
                isCashSent.value = false
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
                    viewDataBinding.progressBar.visibility = View.VISIBLE
                    viewModel.transferCash(arguments.getParcelable(ARGUMENT_FRIEND),
                            viewDataBinding.editTextTransferAmount.text.toString().toFloat(),
                            viewDataBinding.editTextNote.text.toString(), currentPassword!!)
                    isCashSent.value = true
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
        const val ARGUMENT_FRIEND = "FRIEND"
        const val ARGUMENT_PASSWORD = "PASSWORD"
        fun newInstance(friend: Friend, password: String?) = CashTransferFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_FRIEND, friend)
                putString(ARGUMENT_PASSWORD, password)
            }
        }

    }
}