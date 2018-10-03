package com.user.ncard.ui.card.namecard

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import com.user.ncard.LifecycleAppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.landing.landing.LandingPageViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import java.net.URI
import javax.inject.Inject

class EditNameCardActivity : LifecycleAppCompatActivity(), Injectable, HasSupportFragmentInjector {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    lateinit var viewModel: LandingPageViewModel

    var confirmAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LandingPageViewModel::class.java)

        if (savedInstanceState == null && intent != null) {
            //we are creating new name card, hence isUpdating = false
            val editNameCardFragment = EditNameCardFragment.newInstance(intent.getParcelableExtra("namecard"),
                    false, false, intent.getSerializableExtra("front") as URI)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, editNameCardFragment, "EditNameCardFragment")
                    .commitAllowingStateLoss()
        }
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    override fun onBackPressed() {
        val editNameCardFragment = fragmentManager.findFragmentByTag("EditNameCardFragment")
        if (editNameCardFragment != null && editNameCardFragment.isVisible) {
            if (confirmAlert == null) confirmAlert = showConfirmAlert(this, getString(R.string.are_you_sure_to_quit))
            else confirmAlert?.show()
        } else super.onBackPressed()
    }

    fun showConfirmAlert(activity: Activity, message: String): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            super.onBackPressed()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        builder.setTitle(R.string.cardline)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }

}