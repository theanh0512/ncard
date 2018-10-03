package com.user.ncard.ui.discovery

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.user.ncard.LifecycleAppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.landing.landing.LandingPageViewModel
import com.user.ncard.util.SharedPreferenceHelper
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class FriendRequestActivity : LifecycleAppCompatActivity(), Injectable, HasSupportFragmentInjector {

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    lateinit var viewModel: LandingPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        if (savedInstanceState == null) {
            val friendRequestFragment = FriendRequestFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, friendRequestFragment)
                    .commitAllowingStateLoss()
        }
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

}