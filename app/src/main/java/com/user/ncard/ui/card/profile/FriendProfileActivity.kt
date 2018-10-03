package com.user.ncard.ui.card.profile

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.landing.landing.LandingPageViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class FriendProfileActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    lateinit var viewModel: LandingPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LandingPageViewModel::class.java)

        if (savedInstanceState == null && intent != null) {
            val addFriendFragment = ProfileFragment.newInstance(intent.getParcelableExtra("user"),
                    intent.getIntExtra(ProfileFragment.ARGUMENT_USER_ID, 0))
            fragmentManager.beginTransaction()
                    .replace(R.id.container, addFriendFragment)
                    .commitAllowingStateLoss()
        }
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

}