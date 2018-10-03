package com.user.ncard.di

import com.user.ncard.ui.me.SettingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class SettingActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentSettingBuildersModule::class))
    abstract fun contributeActivity(): SettingActivity
}