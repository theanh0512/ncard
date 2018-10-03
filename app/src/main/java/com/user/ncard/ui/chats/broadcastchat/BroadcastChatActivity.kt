package com.user.ncard.ui.chats.broadcastchat

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.vo.BroadcastGroup
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Created by dangui on 13/11/17.
 */
class BroadcastChatActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {

    companion object {
        fun getIntent(context: Context, broadcastGroup: BroadcastGroup): Intent = Intent(context, BroadcastChatActivity::class.java).
                putExtra("broadcastGroup", broadcastGroup)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)


        fragmentManager = this.supportFragmentManager

        if (savedInstanceState == null && intent != null) {
            //new ChatFragment
            val fragment = BroadcastChatFragment.newInstance()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commitAllowingStateLoss()
        }
    }

    fun getFragment(): Fragment {
        return supportFragmentManager
                .findFragmentById(R.id.container) as BroadcastChatFragment
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector;
    }

    override fun onBackPressed() {
        (getFragment() as BroadcastChatFragment)?.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fragment = getFragment()
            fragment?.onActivityResult(requestCode, resultCode, data)

        }
    }

}