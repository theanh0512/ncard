package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatActivity
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class BroadcastChatActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(BroadcastChatFragmentBuildersModule::class))
    abstract fun contributeActivity(): BroadcastChatActivity
}