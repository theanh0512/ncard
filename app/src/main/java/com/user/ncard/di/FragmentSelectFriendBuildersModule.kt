package com.user.ncard.di

import com.user.ncard.ui.me.ewallet.SelectFriendFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentSelectFriendBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSelectFriendFragment(): SelectFriendFragment
}