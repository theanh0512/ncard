package com.user.ncard.di

import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.discovery.FriendRecommendationFragment
import com.user.ncard.ui.discovery.FriendRequestFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentFriendRecommendationBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeFriendRecommendationFragment(): FriendRecommendationFragment
}