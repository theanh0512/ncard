package com.user.ncard.di

import com.user.ncard.ui.landing.confirmaccount.ConfirmAccountFragment
import com.user.ncard.ui.landing.createaccount.CreateAccountFragment
import com.user.ncard.ui.landing.inputinfo.InputInfoFragment
import com.user.ncard.ui.landing.signin.ForgetPasswordCodeFragment
import com.user.ncard.ui.landing.signin.ForgetPasswordFragment
import com.user.ncard.ui.landing.signin.SetPaymentPasswordFragment
import com.user.ncard.ui.landing.signin.SignInFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeCreateAccountFragment(): CreateAccountFragment

    @ContributesAndroidInjector
    abstract fun contributeConfirmAccountFragment(): ConfirmAccountFragment

    @ContributesAndroidInjector
    abstract fun contributeInputInfoFragment(): InputInfoFragment

    @ContributesAndroidInjector
    abstract fun contributeSignInFragment(): SignInFragment

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordFragment(): ForgetPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordCodeFragment(): ForgetPasswordCodeFragment

    @ContributesAndroidInjector
    abstract fun contributeSetPaymentPasswordFragment(): SetPaymentPasswordFragment
}
