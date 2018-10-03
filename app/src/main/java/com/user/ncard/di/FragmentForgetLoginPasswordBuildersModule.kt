package com.user.ncard.di

import com.user.ncard.ui.landing.signin.ForgetPasswordCodeFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentForgetLoginPasswordBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeForgetPasswordCodeFragment(): ForgetPasswordCodeFragment
}