package com.user.ncard.di

import com.user.ncard.ui.card.namecard.NameCardMoreActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define NameCardMoreActivity-specific dependencies here.
 */
@Module
abstract class NameCardMoreActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentNameCardMoreBuildersModule::class))
    abstract fun contributeNameCardMoreActivity(): NameCardMoreActivity
}