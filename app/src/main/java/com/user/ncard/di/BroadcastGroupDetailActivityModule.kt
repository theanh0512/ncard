package com.user.ncard.di

import com.user.ncard.ui.chats.broadcast.BroadcastGroupListActivity
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class BroadcastGroupDetailActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(BroadcastGroupDetailFragmentBuildersModule::class))
    abstract fun contributeActivity(): BroadcastGroupDetailActivity
}