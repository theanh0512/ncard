package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatActivity
import com.user.ncard.ui.me.AccountActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class AccountActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(AccountFragmentBuildersModule::class))
    abstract fun contributeActivity(): AccountActivity
}