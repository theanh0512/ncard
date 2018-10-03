package com.user.ncard.di

import com.user.ncard.ui.group.GroupActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 7/11/17.
 * Define GroupActivity-specific dependencies here.
 */
@Module
abstract class GroupActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentGroupBuildersModule::class))
    abstract fun contributeGroupActivity(): GroupActivity
}