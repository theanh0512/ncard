package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatActivity
import com.user.ncard.ui.chats.forward.ForwardListActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ChatForwardListActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(ChatForwardListFragmentBuildersModule::class))
    abstract fun contributeActivity(): ForwardListActivity
}