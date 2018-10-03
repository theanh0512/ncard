package com.user.ncard.di

import com.user.ncard.ui.me.ewallet.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentEWalletBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFragment(): EWalletFragment

    @ContributesAndroidInjector
    abstract fun contributePaymentMethodFragment(): TopUpFragment

    @ContributesAndroidInjector
    abstract fun contributePaymentSecurityFragment(): PaymentSecurityFragment

    @ContributesAndroidInjector
    abstract fun contributeSetPaymentPasswordFragment(): SetPaymentPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordFragment(): ForgetPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordCodeFragment(): ForgetPasswordCodeFragment

    @ContributesAndroidInjector
    abstract fun contributeCashTransferFragment(): CashTransferFragment

    @ContributesAndroidInjector
    abstract fun contributeTransactionLogFragment(): TransactionLogFragment

    @ContributesAndroidInjector
    abstract fun contributeTransactionLogDetailFragment(): TransactionLogDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeWithdrawFragment(): WithdrawFragment
}