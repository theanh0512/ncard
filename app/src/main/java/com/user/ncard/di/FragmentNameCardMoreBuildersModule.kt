package com.user.ncard.di

import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.card.namecard.NameCardMoreFragment
import com.user.ncard.ui.card.namecard.SetRemarkNameCardFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentNameCardMoreBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeNameCardMoreFragment(): NameCardMoreFragment

    @ContributesAndroidInjector
    abstract fun contributeEditNameCardFragment(): EditNameCardFragment

    @ContributesAndroidInjector
    abstract fun contributeSetRemarkNameCardFragment(): SetRemarkNameCardFragment
}