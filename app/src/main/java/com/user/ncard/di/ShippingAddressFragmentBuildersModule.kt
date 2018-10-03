package com.user.ncard.di

import com.user.ncard.ui.chats.broadcastchat.BroadcastChatFragment
import com.user.ncard.ui.chats.shipping.ShippingAddressFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class ShippingAddressFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): ShippingAddressFragment
}