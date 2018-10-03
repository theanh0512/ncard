package com.user.ncard.ui.card.namecard

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R
import com.user.ncard.ui.landing.landing.LandingPageViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class NameCardMoreActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    lateinit var viewModel: LandingPageViewModel

    var confirmAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        if (savedInstanceState == null && intent != null) {
            //for navigation in in name card more fragment
            val nameCardMoreFragment = NameCardMoreFragment.newInstance(intent.getParcelableExtra("namecard"),
                    intent.getBooleanExtra("isMyNameCard", false), intent.getBooleanExtra("fromJobIcon", false))
            fragmentManager.beginTransaction()
                    .replace(R.id.container, nameCardMoreFragment)
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