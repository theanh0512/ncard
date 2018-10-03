package com.user.ncard.di

import com.user.ncard.ui.group.SelectGroupItemActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define SelectGroupItemActivity-specific dependencies here.
 */
@Module
abstract class SelectGroupItemActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentSelectGroupItemBuildersModule::class))
    abstract fun contributeSelectGroupItemActivity(): SelectGroupItemActivity
}