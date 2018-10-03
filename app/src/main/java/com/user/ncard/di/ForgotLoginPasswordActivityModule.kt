package com.user.ncard.di

import com.user.ncard.ui.landing.signin.ForgetPasswordActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define ForgetPasswordActivity-specific dependencies here.
 */
@Module
abstract class ForgotLoginPasswordActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentForgetLoginPasswordBuildersModule::class))
    abstract fun contributeForgetPasswordActivity(): ForgetPasswordActivity
}