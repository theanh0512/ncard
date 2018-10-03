package com.user.ncard.ui.chats.broadcastdetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.user.ncard.LifecycleAppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.chats.friends.FriendsListFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class BroadcastGroupDetailActivity : LifecycleAppCompatActivity(), Injectable, HasSupportFragmentInjector {

    companion object {
        fun getIntent(context: Context, type: Int, groupId: Int): Intent = Intent(context, BroadcastGroupDetailActivity::class.java).
                putExtra("type", type).putExtra("groupId", groupId)
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            val fragment = BroadcastGroupDetailFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
        }
    }

    fun getFragment(): Fragment {
        return supportFragmentManager
                .findFragmentById(R.id.container) as BroadcastGroupDetailFragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fragment = getFragment()
            fragment?.onActivityResult(requestCode, resultCode, data)

        }
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }
}