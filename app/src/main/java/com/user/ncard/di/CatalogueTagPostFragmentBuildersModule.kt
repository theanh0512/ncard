package com.user.ncard.di

import com.user.ncard.ui.card.catalogue.share.SharePostFragment
import com.user.ncard.ui.card.catalogue.tag.TagPostFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by trong-android-dev on 20/10/17.
 */
@Module
abstract class CatalogueTagPostFragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeFragment(): TagPostFragment
}