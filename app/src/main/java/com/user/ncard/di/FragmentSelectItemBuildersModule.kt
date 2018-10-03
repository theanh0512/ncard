package com.user.ncard.di

import com.user.ncard.ui.card.SelectItemFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentSelectItemBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSelectItemFragment(): SelectItemFragment
}