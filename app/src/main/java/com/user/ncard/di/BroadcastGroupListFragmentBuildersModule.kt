package com.user.ncard.di

import com.user.ncard.ui.catalogue.detail.CatalogueDetailFragment
import com.user.ncard.ui.chats.broadcast.BroacastGroupListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class BroadcastGroupListFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): BroacastGroupListFragment
}