package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatFragment
import com.user.ncard.ui.chats.forward.ForwardListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ChatForwardListFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): ForwardListFragment
}