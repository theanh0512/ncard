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
import com.user.ncard.ui.me.ewallet.CashTransferFragment
import com.user.ncard.vo.Friend
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Created by dangui on 13/11/17.
 */
class CashTransferActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {

    companion object {
        fun getIntent(context: Context, friend: Friend, password: String?): Intent = Intent(context, CashTransferActivity::class.java).
                putExtra(CashTransferFragment.ARGUMENT_FRIEND, friend).putExtra(CashTransferFragment.ARGUMENT_PASSWORD, password)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        val friend = intent.getParcelableExtra<Friend>(CashTransferFragment.ARGUMENT_FRIEND) as Friend
        val password: String? = intent.getStringExtra(CashTransferFragment.ARGUMENT_PASSWORD)

        fragmentManager = this.supportFragmentManager

        if (savedInstanceState == null && intent != null) {
            //new ChatFragment
            val cashFragment = CashTransferFragment.newInstance(friend, password)
            //pass the chatDialog
            fragmentManager.beginTransaction()
                    .replace(R.id.container, cashFragment)
                    .commitAllowingStateLoss()
        }
    }

    fun getFragment(): Fragment {
        return supportFragmentManager
                .findFragmentById(R.id.container) as CashTransferFragment
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fragment = getFragment()
            fragment?.onActivityResult(requestCode, resultCode, data)

        }
    }

}