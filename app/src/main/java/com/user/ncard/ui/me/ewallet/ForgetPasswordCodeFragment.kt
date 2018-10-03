package com.user.ncard.ui.me.ewallet

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
import com.user.ncard.databinding.FragmentForgetPasswordCodeBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import javax.inject.Inject

class ForgetPasswordCodeFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentForgetPasswordCodeBinding

    lateinit var wrongPasswordAlert: AlertDialog
    var isCorrectCode = false
    lateinit var serverCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_forget_password_code, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.empty_string)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)
        viewDataBinding.editCodeView.requestFocus()
        serverCode = arguments.getString(ARGUMENT_CODE)
        viewDataBinding.editCodeView.setEditCodeListener { code ->
            isCorrectCode = code == serverCode
        }
        viewDataBinding.buttonSubmit.setOnClickListener {
            if (isCorrectCode) {
                val setPaymentPasswordFragment = SetPaymentPasswordFragment.newInstance(true, "")
                fragmentManager.beginTransaction()
                        .replace(R.id.container, setPaymentPasswordFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            } else {
                wrongPasswordAlert = Utils.showAlert(activity, getString(R.string.wrong_password))
            }
        }
        viewDataBinding.buttonResend.setOnClickListener {
            viewModel.forgetPaymentPassword()
            viewDataBinding.buttonSubmit.apply {
                isEnabled = false
                setBackgroundResource(R.drawable.rounded_corner_button_disabled)
            }
            viewDataBinding.progressBar.visibility = View.VISIBLE
        }

        viewModel.forgetPasswordResponse.observe(this@ForgetPasswordCodeFragment, Observer { respsonse ->
            if (respsonse != null) {
                viewDataBinding.progressBar.visibility = View.GONE
                serverCode = respsonse.tempPassword
                viewDataBinding.buttonSubmit.apply {
                    isEnabled = true
                    setBackgroundResource(R.drawable.rounded_corner_button)
                }
            }
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
        const val ARGUMENT_CODE = "CODE"

        fun newInstance(code: String) = ForgetPasswordCodeFragment().apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_CODE, code)
            }
        }

    }
}