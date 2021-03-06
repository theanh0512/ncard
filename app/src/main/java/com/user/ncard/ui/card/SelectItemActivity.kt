package com.user.ncard.ui.card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R
import com.user.ncard.vo.BaseEntity
import com.user.ncard.vo.NameCard
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class SelectItemActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        val friend = intent.getParcelableExtra<BaseEntity>("friend")
        val nameCard = intent.getParcelableExtra<NameCard>("nameCard")
        val selectItemFragment = SelectItemFragment.newInstance(friend,nameCard)
        fragmentManager.beginTransaction()
                .replace(R.id.container, selectItemFragment)
                .commitAllowingStateLoss()
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }
}