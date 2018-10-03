package com.user.ncard.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager

import com.user.ncard.NCardApplication

import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector

/**
 * AppInjector is a helper class to automatically inject fragments if they implement [Injectable].
 */
class AppInjector {

    companion object {
        fun init(nCardApplication: NCardApplication) {
            DaggerAppComponent.builder().application(nCardApplication)
                    .build().inject(nCardApplication)

            nCardApplication
                    .registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                            handleActivity(activity)
                        }

                        override fun onActivityStarted(activity: Activity) {

                        }

                        override fun onActivityResumed(activity: Activity) {

                        }

                        override fun onActivityPaused(activity: Activity) {

                        }

                        override fun onActivityStopped(activity: Activity) {

                        }

                        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {

                        }

                        override fun onActivityDestroyed(activity: Activity) {

                        }
                    })
        }

        private fun handleActivity(activity: Activity) {
            if (activity is HasSupportFragmentInjector) {
                AndroidInjection.inject(activity)
            }
            if (activity is Injectable) {
                AndroidInjection.inject(activity)
            }
            (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
                    object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentCreated(fm: FragmentManager?, fragment: Fragment?,
                                                       savedInstanceState: Bundle?) {
                            if (fragment is Injectable) {
                                AndroidSupportInjection.inject(fragment)
                            }
                        }
                    }, true)
        }
    }


}
