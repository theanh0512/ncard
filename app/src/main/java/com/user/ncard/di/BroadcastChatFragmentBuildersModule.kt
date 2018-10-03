package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatFragment
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class BroadcastChatFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): BroadcastChatFragment
}