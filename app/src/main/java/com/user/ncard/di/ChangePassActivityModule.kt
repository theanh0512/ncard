package com.user.ncard.di

import com.user.ncard.ui.me.AccountActivity
import com.user.ncard.ui.me.ChangePassActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ChangePassActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(ChangePassFragmentBuildersModule::class))
    abstract fun contributeActivity(): ChangePassActivity
}