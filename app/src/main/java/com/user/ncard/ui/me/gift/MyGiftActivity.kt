package com.user.ncard.ui.me.gift

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.vo.Friend
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class MyGiftActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager


        val friend: Friend? = if (intent.extras != null) intent.getParcelableExtra<Friend>(MyGiftFragment.ARGUMENT_FRIEND) else null
        val fromScreen: String? = if (intent.extras != null) intent.getStringExtra(MyGiftFragment.ARGUMENT_FROM_SCREEN) else null

        if (savedInstanceState == null && intent != null) {
            val myGiftFragment = MyGiftFragment.newInstance(friend, fromScreen)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, myGiftFragment)
                    .commitAllowingStateLoss()
        }
    }


    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

}