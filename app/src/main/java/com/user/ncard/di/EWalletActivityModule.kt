package com.user.ncard.di

import com.user.ncard.ui.group.GroupActivity
import com.user.ncard.ui.me.ewallet.EWalletActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Pham on 7/11/17.
 * Define EWalletActivity-specific dependencies here.
 */
@Module
abstract class EWalletActivityModule {
    @ContributesAndroidInjector(modules = arrayOf(FragmentEWalletBuildersModule::class))
    abstract fun contributeEWalletActivity(): EWalletActivity
}