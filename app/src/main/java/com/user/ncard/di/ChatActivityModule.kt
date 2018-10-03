package com.user.ncard.di

import com.user.ncard.ui.chats.detail.ChatActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by dangui on 13/11/17.
 */

@Module
abstract class ChatActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentChatBuilderModule::class))
    abstract fun contributeChatActivity(): ChatActivity
}