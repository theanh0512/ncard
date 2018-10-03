package com.user.ncard.di

import com.user.ncard.ui.card.profile.FriendProfileActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define FriendProfileActivity-specific dependencies here.
 */
@Module
abstract class FriendProfileActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentFriendProfileBuildersModule::class))
    abstract fun contributeFriendProfileActivity(): FriendProfileActivity
}