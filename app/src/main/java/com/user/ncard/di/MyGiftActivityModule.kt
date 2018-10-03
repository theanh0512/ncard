package com.user.ncard.di

import com.user.ncard.ui.me.gift.MyGiftActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 7/11/17.
 * Define MyGiftActivity-specific dependencies here.
 */
@Module
abstract class MyGiftActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentMyGiftBuildersModule::class))
    abstract fun contributeMyGiftActivity(): MyGiftActivity
}