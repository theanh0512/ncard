package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatActivity
import com.user.ncard.ui.chats.shipping.ShippingAddressActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ShippingAddressActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(ShippingAddressFragmentBuildersModule::class))
    abstract fun contributeActivity(): ShippingAddressActivity
}