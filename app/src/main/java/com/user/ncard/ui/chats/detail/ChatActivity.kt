package com.user.ncard.ui.chats.detail

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.quickblox.chat.model.QBChatDialog
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.catalogue.main.CatalogueMainFragment
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Created by dangui on 13/11/17.
 */
class ChatActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {

    companion object {
        const val EXTRA_DIALOG_ID = "dialogId"
        fun getIntent(context: Context, dialogId: String): Intent = Intent(context, ChatActivity::class.java).
                putExtra(EXTRA_DIALOG_ID, dialogId)
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
            val chatFragment = ChatFragment.newInstance()
            //pass the chatDialog
            fragmentManager.beginTransaction()
                    .replace(R.id.container, chatFragment)
                    .commitAllowingStateLoss()
        }
    }

    fun getFragment(): Fragment {
        return supportFragmentManager
                .findFragmentById(R.id.container) as ChatFragment
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector;
    }

    override fun onBackPressed() {
        (getFragment() as ChatFragment)?.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fragment = getFragment()
            fragment?.onActivityResult(requestCode, resultCode, data)

        }
    }

}