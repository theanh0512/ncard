package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.post.CataloguePostActivity
import com.user.ncard.ui.card.catalogue.post.CataloguePostFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CataloguePostActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(CataloguePostFragmentBuildersModule::class))
    abstract fun contributeActivity(): CataloguePostActivity
}