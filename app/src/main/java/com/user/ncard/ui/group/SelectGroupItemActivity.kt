package com.user.ncard.ui.group

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import com.user.ncard.R
import com.user.ncard.vo.Friend
import com.user.ncard.vo.NameCard
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class SelectGroupItemActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        fragmentManager = this.supportFragmentManager

        val selectedFriend = intent.getParcelableArrayListExtra<Friend>("friend")
        val selectedNameCard = intent.getParcelableArrayListExtra<NameCard>("namecard")
        val selectGroupItemFragment = SelectGroupItemFragment.newInstance(selectedFriend, selectedNameCard)
        fragmentManager.beginTransaction()
                .replace(R.id.container, selectGroupItemFragment)
                .commitAllowingStateLoss()
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }
}