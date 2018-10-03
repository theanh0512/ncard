package com.user.ncard.di

import com.user.ncard.ui.filter.FilterActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define FilterActivity-specific dependencies here.
 */
@Module
abstract class FilterActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentFilterBuildersModule::class))
    abstract fun contributeFilterActivity(): FilterActivity
}