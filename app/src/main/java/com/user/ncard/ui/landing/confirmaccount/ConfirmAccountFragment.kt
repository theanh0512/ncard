package com.user.ncard.ui.landing.confirmaccount

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentConfirmAccountBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.landing.inputinfo.InputInfoFragment
import com.user.ncard.ui.landing.signin.ForgetPasswordFragment
import com.user.ncard.ui.me.MeViewModel
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ConfirmAccountFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: ConfirmAccountViewModel
    lateinit var meViewModel: MeViewModel
    private lateinit var viewDataBinding: FragmentConfirmAccountBinding

    lateinit var signingInAlert: AlertDialog

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_confirm_account, container, false)!!
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ConfirmAccountViewModel::class.java)
        meViewModel = ViewModelProviders.of(this, viewModelFactory).get(MeViewModel::class.java)
        viewModel.username.set(arguments.getString(ARGUMENT_USERNAME))
        viewModel.password.set(arguments.getString(ARGUMENT_PASSWORD))
        viewDataBinding.editCodeView.setEditCodeListener { code ->
            viewModel.code.set(code)
        }
        viewDataBinding.viewmodel = viewModel
        viewModel.apply {
            confirmEvent.observe(this@ConfirmAccountFragment, Observer {
                showProgress.set(false)
                showDialogMessage()
            })
            signInEvent.observe(this@ConfirmAccountFragment, Observer {
                signingInAlert.cancel()
                // Process logout
                val signout_remain_db = sharedPreferenceHelper.getBoolean(SharedPreferenceHelper.Key.SIGNOUT_REMAIN_DB)
                if (signout_remain_db) {
                    processLogout()
                }
                //get user id to save to shared preference
                viewModel.getUserInfo(object: Callback<User> {
                    override fun onFailure(call: Call<User>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<User>?, response: Response<User>?) {
                        val inputInfoFragment = InputInfoFragment()
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, inputInfoFragment, "InputInfoFragment")
                                .addToBackStack(null)
                                .commitAllowingStateLoss()
                    }
                })
            })
            signInFailureEvent.observe(this@ConfirmAccountFragment, Observer {
                signingInAlert.cancel()
                showWrongPasswordDialogMessage()
            })
            confirmResend.observe(this@ConfirmAccountFragment, Observer {
                viewDataBinding.textViewCodeSent.text = getString(R.string.code_resent)
            })
            wrongCodeEvent.observe(this@ConfirmAccountFragment, Observer {
                showProgress.set(false)
                viewDataBinding.textViewCodeSent.text = getString(R.string.incorrect_verification_code)
                viewDataBinding.editCodeView.clearCode()
            })
        }

        initOldEmail()
    }

    fun showDialogMessage() {
        val confirmAlert = AlertDialog.Builder(activity).create()
        confirmAlert.setTitle("Account Confirm")
        confirmAlert.setMessage("Account has been confirmed")

        confirmAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { _, _ ->
            run {
                signingInAlert = Utils.showAlert(activity)
                viewModel.signIn()
            }
        })

        confirmAlert.show()
    }

    fun showWrongPasswordDialogMessage() {
        val confirmAlert = AlertDialog.Builder(activity).create()
        confirmAlert.setTitle("Sign in Problem")
        confirmAlert.setMessage("Wrong password. Please sign in again")

        confirmAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", { _, _ ->
            run {
                confirmAlert.cancel()
                activity.onBackPressed()
            }
        })

        confirmAlert.show()
    }

    var oldEmail: String? = null

    fun initOldEmail() {
        oldEmail = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)
    }

    fun processLogout() {
        val newEmail: String? = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)
        if(oldEmail != null && oldEmail == newEmail) {
            // Still old account -> Do nothing
        } else {
            // New account -> logout
            meViewModel.logout()
        }
    }

    companion object {
        const val ARGUMENT_USERNAME = "USERNAME"
        const val ARGUMENT_PASSWORD = "PASSWORD"

        fun newInstance(userName: String, password: String) = ConfirmAccountFragment().apply {
            arguments = Bundle().apply {
                putString(ARGUMENT_USERNAME, userName)
                putString(ARGUMENT_PASSWORD, password)
            }
        }

    }
}