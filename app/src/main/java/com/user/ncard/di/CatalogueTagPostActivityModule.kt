package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.share.SharePostActivity
import com.user.ncard.ui.card.catalogue.tag.TagPostActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueTagPostActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(CatalogueTagPostFragmentBuildersModule::class))
    abstract fun contributeActivity(): TagPostActivity
}