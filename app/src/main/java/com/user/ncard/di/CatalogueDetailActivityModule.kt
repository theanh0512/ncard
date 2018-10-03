package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.main.CatalogueMainActivity
import com.user.ncard.ui.catalogue.detail.CatalogueDetailActivity
import com.user.ncard.ui.catalogue.my.CatalogueMeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueDetailActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(CatalogueDetailFragmentBuildersModule::class))
    abstract fun contributeActivity(): CatalogueDetailActivity
}