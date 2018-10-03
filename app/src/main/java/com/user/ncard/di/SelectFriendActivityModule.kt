package com.user.ncard.di

import com.user.ncard.ui.me.ewallet.SelectFriendActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 14/12/17.
 * Define SelectItemActivity-specific dependencies here.
 */
@Module
abstract class SelectFriendActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentSelectFriendBuildersModule::class))
    abstract fun contributeSelectFriendActivity(): SelectFriendActivity
}