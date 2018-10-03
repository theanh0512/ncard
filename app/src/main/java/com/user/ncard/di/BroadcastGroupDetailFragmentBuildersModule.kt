package com.user.ncard.di

import com.user.ncard.ui.chats.broadcast.BroacastGroupListFragment
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class BroadcastGroupDetailFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): BroadcastGroupDetailFragment
}