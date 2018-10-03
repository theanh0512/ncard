package com.user.ncard.di

import com.user.ncard.ui.catalogue.my.CatalogueMeFragment
import com.user.ncard.ui.me.ewallet.TopUpFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class TopUpFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): TopUpFragment
}