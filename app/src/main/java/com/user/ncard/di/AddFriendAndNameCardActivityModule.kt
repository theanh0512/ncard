package com.user.ncard.di

import com.user.ncard.ui.card.AddFriendAndNameCardActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define AddFriendAndNameCardActivity-specific dependencies here.
 */
@Module
abstract class AddFriendAndNameCardActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentAddFriendAndNameCardBuildersModule::class))
    abstract fun contributeAddFriendAndNameCardActivity(): AddFriendAndNameCardActivity
}