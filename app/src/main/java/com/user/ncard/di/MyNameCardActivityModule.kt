package com.user.ncard.di

import com.user.ncard.ui.me.MyNameCardActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class MyNameCardActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(MyNameCardFragmentBuildersModule::class))
    abstract fun contributeActivity(): MyNameCardActivity
}