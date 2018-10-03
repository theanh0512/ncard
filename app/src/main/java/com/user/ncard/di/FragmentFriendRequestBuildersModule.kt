package com.user.ncard.di

import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.discovery.FriendRequestFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentFriendRequestBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFriendRequestFragment(): FriendRequestFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment
}