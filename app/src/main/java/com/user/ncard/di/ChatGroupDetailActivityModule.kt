package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailActivity
import com.user.ncard.ui.chats.groups.ChatGroupDetailActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ChatGroupDetailActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(ChatGroupDetailFragmentBuildersModule::class))
    abstract fun contributeActivity(): ChatGroupDetailActivity
}