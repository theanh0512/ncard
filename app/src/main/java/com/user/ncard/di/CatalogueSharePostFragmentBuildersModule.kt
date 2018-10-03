package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.post.CataloguePostFragment
import com.user.ncard.ui.card.catalogue.share.SharePostFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueSharePostFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): SharePostFragment
}