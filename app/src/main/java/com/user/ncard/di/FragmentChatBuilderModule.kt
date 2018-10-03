package com.user.ncard.di

import com.user.ncard.ui.chats.detail.ChatFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by dangui on 13/11/17.
 */
@Module
abstract class FragmentChatBuilderModule {
    @ContributesAndroidInjector
    abstract fun contributeChatFragment(): ChatFragment
}