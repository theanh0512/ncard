package com.user.ncard.di

import com.user.ncard.MainActivity
import com.user.ncard.ui.landing.LandingPageActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 3/8/17.
 * Define MainActivity-specific dependencies here.
 */
@Module
abstract class LandingPageActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentBuildersModule::class))
    abstract fun contributeLandingPageActivity(): LandingPageActivity
}
