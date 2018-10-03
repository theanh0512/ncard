package com.user.ncard.di

import com.user.ncard.ui.discovery.FriendRecommendationActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define MainActivity-specific dependencies here.
 */
@Module
abstract class FriendRecommendationActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentFriendRecommendationBuildersModule::class))
    abstract fun contributeFriendRecommendationActivity(): FriendRecommendationActivity
}