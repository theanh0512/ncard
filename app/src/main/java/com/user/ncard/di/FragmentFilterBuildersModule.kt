package com.user.ncard.di

import com.user.ncard.ui.filter.FilterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentFilterBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFilterFragment(): FilterFragment
}