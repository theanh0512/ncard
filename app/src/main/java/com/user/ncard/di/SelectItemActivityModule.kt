package com.user.ncard.di

import com.user.ncard.ui.card.SelectItemActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 14/12/17.
 * Define SelectItemActivity-specific dependencies here.
 */
@Module
abstract class SelectItemActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentSelectItemBuildersModule::class))
    abstract fun contributeSelectItemActivity(): SelectItemActivity
}