package com.user.ncard.di

import com.user.ncard.ui.card.profile.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentFriendProfileBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileMoreFragment(): ProfileMoreFragment

    @ContributesAndroidInjector
    abstract fun contributeSetRemarkFragment(): SetRemarkFragment

    @ContributesAndroidInjector
    abstract fun contributeUserJobFragment(): UserJobFragment

    @ContributesAndroidInjector
    abstract fun contributeFriendNameCardFragment(): FriendNameCardFragment
}