package com.user.ncard.di

import com.user.ncard.ui.me.MyJobActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MyJobActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(MyJobFragmentBuildersModule::class))
    abstract fun contributeActivity(): MyJobActivity
}