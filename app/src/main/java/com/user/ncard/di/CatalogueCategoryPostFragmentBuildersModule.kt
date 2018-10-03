package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.share.SharePostFragment
import com.user.ncard.ui.catalogue.category.CategoryPostFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueCategoryPostFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): CategoryPostFragment
}