package com.user.ncard.di

import com.user.ncard.ui.me.MyProfileActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define MyProfileActivity-specific dependencies here.
 */
@Module
abstract class MyProfileActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentMyProfileBuildersModule::class))
    abstract fun contributeMyProfileActivity(): MyProfileActivity
}