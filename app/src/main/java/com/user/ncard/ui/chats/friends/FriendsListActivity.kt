package com.user.ncard.ui.chats.friends

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.user.ncard.LifecycleAppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.my.CatalogueMeFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class FriendsListActivity : LifecycleAppCompatActivity(), Injectable, HasSupportFragmentInjector {

    companion object {
        const val CHAT_DIALOG_LIST = 0
        const val BROADCAST_GROUP_DETAIL = 1
        const val FORWARD_MESSAGE = 3
        const val CHAT_GROUP_MANAGE_AS_ADMIN = 4 // can uncheck the friends
        const val CHAT_GROUP_MANAGE_AS_MEMBER = 4 // cannot uncheck the friends
        // occupants for select group chat
        fun getIntent(context: Context, from: Int, friendsSelected: String?, occupants: String?): Intent = Intent(context, FriendsListActivity::class.java).
                putExtra("from", from).putExtra("friendsSelected", friendsSelected).putExtra("occupants", occupants)
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            val fragment = FriendsListFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
        }
    }

    fun getFragment(): Fragment {
        return supportFragmentManager
                .findFragmentById(R.id.container) as FriendsListFragment
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