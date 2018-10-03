package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatActivity
import com.user.ncard.ui.chats.detail.CashTransferActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CashTransferActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(CashTransferFragmentBuildersModule::class))
    abstract fun contributeActivity(): CashTransferActivity
}