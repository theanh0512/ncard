package com.user.ncard.di

import com.user.ncard.ui.me.ewallet.CashTransferFragment
import com.user.ncard.ui.me.ewallet.ForgetPasswordCodeFragment
import com.user.ncard.ui.me.ewallet.ForgetPasswordFragment
import com.user.ncard.ui.me.ewallet.SetPaymentPasswordFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CashTransferFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): CashTransferFragment

    @ContributesAndroidInjector
    abstract fun contributeForgotPasswordFragment(): ForgetPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordCodeFragment(): ForgetPasswordCodeFragment

    @ContributesAndroidInjector
    abstract fun contributeSetPaymentPasswordFragment(): SetPaymentPasswordFragment
}