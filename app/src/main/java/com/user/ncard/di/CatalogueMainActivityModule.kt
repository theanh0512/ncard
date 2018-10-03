package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.main.CatalogueMainActivity
import com.user.ncard.ui.card.catalogue.post.CataloguePostActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueMainActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(CatalogueMainFragmentBuildersModule::class))
    abstract fun contributeActivity(): CatalogueMainActivity
}