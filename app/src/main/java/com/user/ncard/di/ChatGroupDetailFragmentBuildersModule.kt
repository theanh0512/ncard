package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailFragment
import com.user.ncard.ui.chats.groups.ChatGroupDetailFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ChatGroupDetailFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): ChatGroupDetailFragment
}