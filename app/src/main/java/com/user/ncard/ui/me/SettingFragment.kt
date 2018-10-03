package com.user.ncard.ui.me

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSettingBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.discovery.DiscoveryNavigation
import com.user.ncard.ui.landing.LandingPageActivity
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import javax.inject.Inject


class SettingFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: MeViewModel
    private lateinit var viewDataBinding: FragmentSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_setting, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.setting)
        initializeButtons()
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MeViewModel::class.java)
    }

    private fun initializeButtons() {
        viewDataBinding.account = object : DiscoveryNavigation(getString(R.string.account), 0) {
            override fun onClick(view: View) {
                startActivity(AccountActivity.getIntent(activity))
            }
        }
        viewDataBinding.buttonLogout.setOnClickListener {
            viewModel.logoutStillRemainDb()
            //launch landing page
            sharedPreferenceHelper.put(SharedPreferenceHelper.Key.EXPIRATION_TIME_LONG, 0.toLong())
            //clear badge
            sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_DISCOVERY_BADGE, 0)
            Functions.navigateToOnLandingActivity(activity)
            activity.finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}