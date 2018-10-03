package com.user.ncard.di

import com.user.ncard.ui.me.ewallet.TopUpActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class TopUpActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(TopUpFragmentBuildersModule::class))
    abstract fun contributeActivity(): TopUpActivity
}