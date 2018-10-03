package com.user.ncard.di

import com.user.ncard.ui.discovery.FriendRequestActivity
import com.user.ncard.ui.landing.LandingPageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define MainActivity-specific dependencies here.
 */
@Module
abstract class FriendRequestActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentFriendRequestBuildersModule::class))
    abstract fun contributeFriendRequestActivity(): FriendRequestActivity
}