package com.user.ncard.ui.landing.signin

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
import com.user.ncard.databinding.FragmentForgetSignInPasswordCodeBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import javax.inject.Inject

class ForgetPasswordCodeFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: SignInViewModel
    private lateinit var viewDataBinding: FragmentForgetSignInPasswordCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_forget_sign_in_password_code, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.empty_string)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SignInViewModel::class.java)
        if (arguments.getString(ARGUMENT_ERROR) != null) showAlert(activity, arguments.getString(ARGUMENT_ERROR))
        else viewDataBinding.editCodeView.requestFocus()

        viewDataBinding.buttonSubmit.setOnClickListener {
            val code = viewDataBinding.editCodeView.code
            if (code.length == 6) {
                val setPaymentPasswordFragment = SetPaymentPasswordFragment.newInstance(code)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, setPaymentPasswordFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        }
        viewDataBinding.buttonResend.setOnClickListener {
            viewDataBinding.progressBar.visibility = View.VISIBLE
            viewDataBinding.buttonSubmit.apply {
                isEnabled = false
                setBackgroundResource(R.drawable.rounded_corner_button_disabled)
                viewModel.forgetPassword(arguments.getString(ARGUMENT_EMAIL))

            }
            viewDataBinding.progressBar.visibility = View.VISIBLE
        }
        viewModel.continueToCode.observe(this@ForgetPasswordCodeFragment, Observer {
            viewDataBinding.progressBar.visibility = View.GONE
            viewDataBinding.buttonSubmit.isEnabled = true
            viewDataBinding.buttonSubmit.setBackgroundResource(R.drawable.rounded_corner_button)
        })
    }

    fun showAlert(activity: Activity, message: String): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.cancel()
            if (message.contains("limit exceeded")) activity.onBackPressed()
            else viewDataBinding.editCodeView.requestFocus()
        }
        builder.setTitle(R.string.cardline)
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
        private const val ARGUMENT_ERROR = "ERROR"
        private const val ARGUMENT_EMAIL = "EMAIL"
        fun newInstance(error: String?, email: String?) = ForgetPasswordCodeFragment().apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_ERROR, error)
                putString(ARGUMENT_EMAIL, email)
            }
        }

    }
}