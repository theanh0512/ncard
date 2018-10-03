package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.share.SharePostActivity
import com.user.ncard.ui.catalogue.category.CategoryPostActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueCategoryPostActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(CatalogueCategoryPostFragmentBuildersModule::class))
    abstract fun contributeActivity(): CategoryPostActivity
}