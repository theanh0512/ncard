package com.user.ncard.di

import com.user.ncard.ui.catalogue.detail.CatalogueDetailActivity
import com.user.ncard.ui.chats.broadcast.BroadcastGroupListActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class BroadcastGroupListActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(BroadcastGroupListFragmentBuildersModule::class))
    abstract fun contributeActivity(): BroadcastGroupListActivity
}