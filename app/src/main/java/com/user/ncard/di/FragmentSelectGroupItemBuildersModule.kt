package com.user.ncard.di

import com.user.ncard.ui.card.namecard.EditNameCardFragment
import com.user.ncard.ui.group.SelectGroupItemFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentSelectGroupItemBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSelectGroupItemFragment(): SelectGroupItemFragment
}