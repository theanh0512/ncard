package com.user.ncard.di

import com.user.ncard.ui.group.CreateGroupFragment
import com.user.ncard.ui.group.GroupFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentGroupBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeGroupFragment(): GroupFragment

    @ContributesAndroidInjector
    abstract fun contributeCreateGroupFragment(): CreateGroupFragment
}