package com.user.ncard.ui.landing

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.di.Injectable
import com.user.ncard.ui.landing.createaccount.CreateAccountFragment
import com.user.ncard.ui.landing.landing.LandingPageFragment
import com.user.ncard.ui.landing.landing.LandingPageViewModel
import com.user.ncard.ui.landing.signin.SignInFragment
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import java.lang.Exception
import javax.inject.Inject

class LandingPageActivity : AppCompatActivity(), LandingPageNavigator, Injectable, HasSupportFragmentInjector {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    lateinit var viewModel: LandingPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        val username = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USERNAME)
        val signout_remain_db = sharedPreferenceHelper.getBoolean(SharedPreferenceHelper.Key.
                SIGNOUT_REMAIN_DB)
        if (!username.isNullOrEmpty() && !signout_remain_db) {
            val tokenExpirationTime = sharedPreferenceHelper.getLong(SharedPreferenceHelper.Key.EXPIRATION_TIME_LONG)
            if (tokenExpirationTime < System.currentTimeMillis()) {
                Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {

                    override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                        goToMain()
                    }
                })
            } else {
                goToMain()
            }
        } else {

            fragmentManager = this.supportFragmentManager

            viewModel = ViewModelProviders.of(this, viewModelFactory).get(LandingPageViewModel::class.java)
            viewModel.apply {
                createNewAccountEvent.observe(this@LandingPageActivity, Observer {
                    this@LandingPageActivity.createNewAccount()
                })
                signInEvent.observe(this@LandingPageActivity, Observer {
                    this@LandingPageActivity.signIn()
                })
            }
            if (savedInstanceState == null) {
                val landingPageFragment = LandingPageFragment()
                fragmentManager.beginTransaction()
                        .replace(R.id.container, landingPageFragment)
                        .commitAllowingStateLoss()
            }
        }
    }

    override fun createNewAccount() {
        val createAccountFragment = CreateAccountFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.container, createAccountFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
    }

    override fun signIn() {
        val signInFragment = SignInFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.container, signInFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
    }

    override fun goToMain() {
        val intent = Intent(this@LandingPageActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        val inputInfoFragment = fragmentManager.findFragmentByTag("InputInfoFragment")
        if (inputInfoFragment != null && inputInfoFragment.isVisible) {
            fragmentManager.popBackStack("ConfirmAccountFragment", 1)
        } else super.onBackPressed()
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

}
