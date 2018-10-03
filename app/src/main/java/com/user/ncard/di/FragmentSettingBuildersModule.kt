package com.user.ncard.di

import com.user.ncard.ui.me.SettingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentSettingBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeSettingFragment(): SettingFragment
}