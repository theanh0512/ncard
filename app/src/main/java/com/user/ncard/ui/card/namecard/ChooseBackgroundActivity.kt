package com.user.ncard.ui.card.namecard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R

class ChooseBackgroundActivity : AppCompatActivity() {

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        if (savedInstanceState == null) {
            val chooseBackgroundFragment = ChooseBackgroundFragment()
            fragmentManager.beginTransaction()
                    .replace(R.id.container, chooseBackgroundFragment)
                    .commitAllowingStateLoss()
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }
}