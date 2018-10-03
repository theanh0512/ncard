package com.user.ncard.ui.catalogue.my

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.user.ncard.LifecycleAppCompatActivity
import com.user.ncard.R
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.catalogue.main.CatalogueMainFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class CatalogueMeActivity : LifecycleAppCompatActivity(), Injectable, HasSupportFragmentInjector {

    companion object {
        fun getIntent(context: Context, category: String?): Intent = Intent(context, CatalogueMeActivity::class.java).
                putExtra("category", category)
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            val fragment = CatalogueMeFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit()
        }
    }

    fun getFragment(): Fragment {
        return supportFragmentManager
                .findFragmentById(R.id.container) as CatalogueMeFragment
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

    override fun onBackPressed() {
        (getFragment() as CatalogueMeFragment)?.onBackPressed()
    }

}