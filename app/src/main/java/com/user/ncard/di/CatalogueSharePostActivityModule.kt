package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.main.CatalogueMainActivity
import com.user.ncard.ui.card.catalogue.share.SharePostActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueSharePostActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(CatalogueSharePostFragmentBuildersModule::class))
    abstract fun contributeActivity(): SharePostActivity
}