package com.user.ncard.ui.me

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentMeBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.my.CatalogueMeActivity
import com.user.ncard.ui.discovery.DiscoveryNavigation
import com.user.ncard.ui.me.ewallet.EWalletActivity
import com.user.ncard.ui.me.gift.MyGiftActivity
import com.user.ncard.ui.me.gift.MyGiftFragment
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.CategoryPost
import com.user.ncard.vo.User
import javax.inject.Inject

class MeFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: MeViewModel
    private lateinit var viewDataBinding: FragmentMeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_me, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
        (activity as AppCompatActivity).setSupportActionBar(viewDataBinding.toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            val inflaterActionbar = LayoutInflater.from(activity)
            val v = inflaterActionbar.inflate(R.layout.action_bar_layout, null)
            val params = ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER)
            actionBar.setCustomView(v, params)
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(R.string.title_me)
//            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).typeface = Utils.getCustomTypeFace(activity, Utils.TypeFaceOption.BRANDON_BLACK)
        }
        initializeButtons()
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MeViewModel::class.java)
        viewDataBinding.containerMe.setOnClickListener {
            val intent = Intent(activity, MyProfileActivity::class.java)
            startActivity(intent)
        }

        initData()
    }

    fun initData() {
        viewModel.getMe()

        viewModel.userData.observe(this, Observer<User> { user ->
            viewDataBinding.user = user
        })
    }

    private fun initializeButtons() {

        viewDataBinding.myjob = object : DiscoveryNavigation(getString(R.string.my_job), R.drawable.ic_job) {
            override fun onClick(view: View) {
                startActivity(Intent(activity, MyJobActivity::class.java))
            }
        }

        viewDataBinding.mynamecard = object : DiscoveryNavigation(getString(R.string.my_name_card), R.drawable.ic_namecard) {
            override fun onClick(view: View) {
                startActivity(Intent(activity, MyNameCardActivity::class.java))
            }
        }

        viewDataBinding.mytimeline = object : DiscoveryNavigation(getString(R.string.my_timeline), R.drawable.catalogue) {
            override fun onClick(view: View) {
                startActivity(CatalogueMeActivity.getIntent(activity, CategoryPost.CategoryPostType.PERSONAL.type))
            }
        }

        viewDataBinding.mybusinesstimeline = object : DiscoveryNavigation(getString(R.string.my_busineess_timeline), R.drawable.catalogue) {
            override fun onClick(view: View) {
                startActivity(CatalogueMeActivity.getIntent(activity, CategoryPost.CategoryPostType.BUSINESS.type))
            }
        }

        viewDataBinding.wallet = object : DiscoveryNavigation(getString(R.string.e_wallet), R.drawable.ic_wallet) {
            override fun onClick(view: View) {
                startActivity(Intent(activity, EWalletActivity::class.java))
            }
        }

        viewDataBinding.gift = object : DiscoveryNavigation(getString(R.string.my_gift_purchased), R.drawable.ic_my_gift) {
            override fun onClick(view: View) {
                startActivity(Intent(activity, MyGiftActivity::class.java).putExtra(MyGiftFragment.ARGUMENT_FROM_SCREEN, MyGiftFragment.FROM_SCREEN_WALLET))
            }
        }

        viewDataBinding.setting = object : DiscoveryNavigation(getString(R.string.setting), R.drawable.ic_setting) {
            override fun onClick(view: View) {
                startActivity(Intent(activity, SettingActivity::class.java))
            }
        }
    }
}