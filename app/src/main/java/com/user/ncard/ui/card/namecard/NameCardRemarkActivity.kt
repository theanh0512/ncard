package com.user.ncard.ui.card.namecard

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R

class NameCardRemarkActivity : AppCompatActivity() {

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        if (savedInstanceState == null) {
            when {
                intent.hasExtra("nationality") -> {
                    val nationalityFragment = NationalityFragment.newInstance(true)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, nationalityFragment)
                            .commitAllowingStateLoss()
                }
                intent.hasExtra("country") -> {
                    val nationalityFragment = NationalityFragment.newInstance(false)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, nationalityFragment)
                            .commitAllowingStateLoss()
                }
                intent.hasExtra("industry") -> {
                    val industryFragment = IndustryFragment()
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, industryFragment)
                            .commitAllowingStateLoss()
                }
            }
        }
    }
}