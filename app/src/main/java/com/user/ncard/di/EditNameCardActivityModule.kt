package com.user.ncard.di

import com.user.ncard.ui.card.namecard.EditNameCardActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define EditNameCardActivity-specific dependencies here.
 */
@Module
abstract class EditNameCardActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentEditNameCardBuildersModule::class))
    abstract fun contributeEditNameCardActivity(): EditNameCardActivity
}