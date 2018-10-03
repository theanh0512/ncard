package com.user.ncard.di

import com.user.ncard.MainActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define MainActivity-specific dependencies here.
 */
@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentMainBuildersModule::class))
    abstract fun contributeMainActivity(): MainActivity
}
