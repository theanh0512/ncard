package com.user.ncard.ui.card.namecard

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R
import com.user.ncard.util.Constants

class FullScreenImageViewActivity : AppCompatActivity() {

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager
        val fullScreenImageViewFragment = FullScreenImageViewFragment.newInstance(ImageSource(intent.getParcelableExtra("uri"),
                intent.getStringExtra("url") ?: "", intent.getIntExtra("resourceId", Constants.BIGGEST_INT)))
        fragmentManager.beginTransaction()
                .replace(R.id.container, fullScreenImageViewFragment)
                .commitAllowingStateLoss()
    }
}