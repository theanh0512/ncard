package com.user.ncard.di

import com.user.ncard.ui.me.AccountFragment
import com.user.ncard.ui.me.ChangePassFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ChangePassFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): ChangePassFragment
}