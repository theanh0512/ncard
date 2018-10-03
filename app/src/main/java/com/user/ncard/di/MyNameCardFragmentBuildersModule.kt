package com.user.ncard.di

import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.me.MyNameCardFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MyNameCardFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): MyNameCardFragment

    @ContributesAndroidInjector
    abstract fun contributeEditNameCardFragment(): EditNameCardFragment
}