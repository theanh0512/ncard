package com.user.ncard.ui.me.ewallet

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.MutableBoolean
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentForgetPasswordBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import javax.inject.Inject

class ForgetPasswordFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: EWalletViewModel
    private lateinit var viewDataBinding: FragmentForgetPasswordBinding

    lateinit var confirmAlert: AlertDialog
    var isConfirmClicked = MutableBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_forget_password, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.space_string)
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EWalletViewModel::class.java)

        viewDataBinding.apply {
            buttonSubmit.setOnClickListener {
                if (editTextEmail.text.toString() != sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL))
                    showDialogMessage()
                else {
                    isConfirmClicked.value = true
                    viewModel.forgetPaymentPassword()
                    viewDataBinding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        viewModel.forgetPasswordResponse.observe(this@ForgetPasswordFragment, Observer { respsonse ->
            if (respsonse != null && isConfirmClicked.value) {
                isConfirmClicked.value = false
                viewDataBinding.progressBar.visibility = View.GONE
                val forgetPasswordCodeFragment = ForgetPasswordCodeFragment.newInstance(respsonse.tempPassword)
                fragmentManager.beginTransaction()
                        .replace(R.id.container, forgetPasswordCodeFragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
            }
        })
    }

    fun showDialogMessage() {
        confirmAlert = AlertDialog.Builder(activity).create()
        confirmAlert.setTitle("Email not matched!")

        confirmAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { _, _ ->
            run {
                confirmAlert.cancel()
            }
        })

        confirmAlert.show()
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
        const val ARGUMENT_USERNAME = "USERNAME"

        fun newInstance() = ForgetPasswordFragment().apply {
            arguments = Bundle().apply {
            }
        }

    }
}