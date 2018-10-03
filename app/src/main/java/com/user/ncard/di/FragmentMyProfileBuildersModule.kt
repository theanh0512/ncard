package com.user.ncard.di

import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.me.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentMyProfileBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeMyProfileFragment(): MyProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeEditMyProfileFragment(): EditMyProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeMyJobFragment(): MyJobFragment

    @ContributesAndroidInjector
    abstract fun contributeEditMyJobFragment(): EditMyJobFragment

    @ContributesAndroidInjector
    abstract fun contributeMyNameCardFragment(): MyNameCardFragment

    @ContributesAndroidInjector
    abstract fun contributeEditNameCardFragment(): EditNameCardFragment
}