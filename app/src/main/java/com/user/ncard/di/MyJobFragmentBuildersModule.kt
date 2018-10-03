package com.user.ncard.di

import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.me.EditMyJobFragment
import com.user.ncard.ui.me.MyJobFragment
import com.user.ncard.ui.me.MyNameCardFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MyJobFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): MyJobFragment

    @ContributesAndroidInjector
    abstract fun contributeEditMyJobFragment(): EditMyJobFragment

    @ContributesAndroidInjector
    abstract fun contributeMyNameCardFragment(): MyNameCardFragment

    @ContributesAndroidInjector
    abstract fun contributeEditNameCardFragment(): EditNameCardFragment
}