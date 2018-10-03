package com.user.ncard.di

import com.user.ncard.ui.chats.friends.FriendsListActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class FriendsListActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FriendsListFragmentBuildersModule::class))
    abstract fun contributeActivity(): FriendsListActivity
}