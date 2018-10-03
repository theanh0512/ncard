package com.user.ncard.di

import com.user.ncard.ui.card.namecard.EditNameCardFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentEditNameCardBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeEditNameCardFragment(): EditNameCardFragment
}