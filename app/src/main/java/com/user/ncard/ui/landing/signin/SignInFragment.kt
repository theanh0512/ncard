package com.user.ncard.ui.landing.signin

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSignInBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.CallbackAlertDialogListener
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.landing.confirmaccount.ConfirmAccountFragment
import com.user.ncard.ui.me.MeViewModel
import com.user.ncard.util.AppHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class SignInFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var meViewModel: MeViewModel
    lateinit var viewModel: SignInViewModel
    private lateinit var viewDataBinding: FragmentSignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_sign_in, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SignInViewModel::class.java)
        meViewModel = ViewModelProviders.of(this, viewModelFactory).get(MeViewModel::class.java)
        viewDataBinding.viewmodel = viewModel
        viewModel.apply {
            signInSuccessEvent.observe(this@SignInFragment, Observer {
                // Process logout
                val signout_remain_db = sharedPreferenceHelper.getBoolean(SharedPreferenceHelper.Key.SIGNOUT_REMAIN_DB)
                if (signout_remain_db) {
                    processLogout()
                }
                viewModel.getUserInfo(object: Callback<User> {
                    override fun onFailure(call: Call<User>?, t: Throwable?) {
                    }

                    override fun onResponse(call: Call<User>?, response: Response<User>?) {
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                        activity.finish()
                    }

                })
            })
            signInFailureEvent.observe(this@SignInFragment, Observer {
                showProgress.set(false)
            })
            userNotConfirmEvent.observe(this@SignInFragment, Observer {
                showProgress.set(false)
                if (viewModel.email?.get() != null && viewModel.password?.get() != null) {
                    val confirmAccountFragment = ConfirmAccountFragment.newInstance(viewModel.email!!.get()!!.toLowerCase(), viewModel.password!!.get()!!)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, confirmAccountFragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                }
            })
        }
        viewDataBinding.textViewForgetPassword.setOnClickListener {
            val forgetPasswordFragment = ForgetPasswordFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, forgetPasswordFragment)
                    .addToBackStack("PaymentSecurityFragment")
                    .commitAllowingStateLoss()
        }

        viewDataBinding.buttonSignIn.setOnClickListener({
            val signout_remain_db = sharedPreferenceHelper.getBoolean(SharedPreferenceHelper.Key.SIGNOUT_REMAIN_DB)
            if (signout_remain_db && sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL) != viewModel.email?.get()) {
                Functions.showAlertDialogYesNo(context, "", "You are trying to login a different account. This will delete all the chat records. Continue?", object : CallbackAlertDialogListener {
                    override fun onClickOk() {
                        viewModel.signIn()
                    }

                    override fun onClickCancel() {
                    }

                })
            } else {
                viewModel.signIn()
            }
        })

        initOldEmail()
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
}