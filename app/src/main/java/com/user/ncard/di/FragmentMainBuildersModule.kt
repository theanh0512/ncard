package com.user.ncard.di

import com.user.ncard.ui.card.CardsFragment
import com.user.ncard.ui.chats.dialogs.ChatListFragment
import com.user.ncard.ui.discovery.DiscoveryFragment
import com.user.ncard.ui.me.MeFragment
import com.user.ncard.ui.me.SettingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentMainBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeMeFragment(): MeFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingFragment(): SettingFragment

    @ContributesAndroidInjector
    abstract fun contributeCardsFragment(): CardsFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoveryFragment(): DiscoveryFragment

    @ContributesAndroidInjector
    abstract fun contributeChatsFragment(): ChatListFragment
}