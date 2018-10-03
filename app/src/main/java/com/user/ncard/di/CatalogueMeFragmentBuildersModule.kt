package com.user.ncard.di

import com.user.ncard.ui.catalogue.my.CatalogueMeFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueMeFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): CatalogueMeFragment
}