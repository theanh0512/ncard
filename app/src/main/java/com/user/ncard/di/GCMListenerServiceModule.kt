package com.user.ncard.di

import com.user.ncard.MainActivity
import com.user.ncard.util.GCMListenerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Trong on 26/12/17.
 * Not work here, need to change structure of AppInjector for support Service
 * Need to inject manually inside GCMListenerService
 */
@Module
abstract class GCMListenerServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeGCMListenerService(): GCMListenerService
}