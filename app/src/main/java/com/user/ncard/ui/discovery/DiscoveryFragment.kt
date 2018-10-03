package com.user.ncard.ui.discovery

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.user.ncard.R
import com.user.ncard.databinding.FragmentDiscoveryBinding
import com.user.ncard.db.NCardInfoDao
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.catalogue.main.CatalogueMainActivity
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.CategoryPost
import com.user.ncard.vo.NCardInfo
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class DiscoveryFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: DiscoveryViewModel
    private lateinit var viewDataBinding: FragmentDiscoveryBinding

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_discovery, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, ContextCompat.getColor(context, R.color.colorDarkerWhite))
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(R.string.title_disscovery)
//            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).typeface = Utils.getCustomTypeFace(activity, Utils.TypeFaceOption.BRANDON_BLACK)
        }
        initializeButtons()
        initFriendRecommendationAndRequest()
        /*viewDataBinding.textViewDiscoveryBadge.apply {
            val badge = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_FRIEND_REQUEST_BADGE, 0)
            visibility = if (badge == 0) View.INVISIBLE else View.VISIBLE
            text = badge.toString()
        }*/
        //viewDataBinding.includeCatalogue.textView.setText(R.string.catalogue)
        return viewDataBinding.root
    }

    private fun initializeButtons() {
        viewDataBinding.friendRequest = object : DiscoveryNavigation(getString(R.string.friend_request), R.drawable.friend_request) {
            override fun onClick(view: View) {
                //reset badge when seen
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_FRIEND_REQUEST_BADGE, 0)
                val intent = Intent(activity, FriendRequestActivity::class.java)
                startActivity(intent)
            }
        }
        viewDataBinding.friendRecommendation = object : DiscoveryNavigation(getString(R.string.recommendation), R.drawable.friend_recommendation) {
            override fun onClick(view: View) {
                val intent = Intent(activity, FriendRecommendationActivity::class.java)
                startActivity(intent)
            }
        }
        viewDataBinding.catalogueBusiness = object : DiscoveryNavigation(getString(R.string.catalogue_business), R.drawable.catalogue) {
            override fun onClick(view: View) {
                startActivity(CatalogueMainActivity.getIntent(activity, CategoryPost.CategoryPostType.BUSINESS.type))
            }
        }
        viewDataBinding.cataloguePersonal = object : DiscoveryNavigation(getString(R.string.catalogue_personal), R.drawable.catalogue) {
            override fun onClick(view: View) {
                startActivity(CatalogueMainActivity.getIntent(activity, CategoryPost.CategoryPostType.PERSONAL.type))
            }
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DiscoveryViewModel::class.java)
//        viewDataBinding.viewmodel = viewModel
//        viewModel.apply {
//            signInSuccessEvent.observe(this@DiscoveryFragment, Observer {
//                val intent = Intent(activity, MainActivity::class.java)
//                startActivity(intent)
//                activity.finish()
//            })
//        }

        // fakeTokenForTesting()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onRequestRecommendationUpdateEvent(requestRecommendationUpdateEvent: RequestRecommendationUpdateEvent) {
        initFriendRecommendationAndRequest()
    }
    var nCardInfoList: LiveData<List<NCardInfo>>? = null
    @Inject lateinit var nCardInfoDao: NCardInfoDao

    fun initFriendRecommendationAndRequest() {
        nCardInfoList = nCardInfoDao.getNCardInfo()
        nCardInfoList?.observe(this, android.arch.lifecycle.Observer {
            if (it != null && it?.isNotEmpty()) {
                val countFriendRequest = it?.get(0).friendRequest!!
                if (countFriendRequest > 0) {
                    viewDataBinding.textViewDiscoveryBadge.visibility = View.VISIBLE
                    viewDataBinding.textViewDiscoveryBadge.text = countFriendRequest.toString()
                } else {
                    viewDataBinding.textViewDiscoveryBadge.visibility = View.GONE
                }
                val countRecommendation = + it?.get(0)?.friendRecommendation!! + it?.get(0)?.cardRecommendation!!
                if (countRecommendation > 0) {
                    viewDataBinding.textViewRecommendationBadge.visibility = View.VISIBLE
                    viewDataBinding.textViewRecommendationBadge.text = countRecommendation.toString()
                } else {
                    viewDataBinding.textViewRecommendationBadge.visibility = View.GONE
                }
            } else {
                viewDataBinding.textViewDiscoveryBadge.visibility = View.GONE
                viewDataBinding.textViewRecommendationBadge.visibility = View.GONE
            }
        })
    }

    fun fakeTokenForTesting() {
        var cookie = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_SESSION)
        var time = System.currentTimeMillis()
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.EXPIRATION_TIME_LONG, time - 6000000)
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CURRENT_SESSION, "eyJraWQiOiJFbytJcEJ1SnY1cmYwdTRabFZwbm1FQytlM3UwSGhQdTdSdkxWTnRZWmpjPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJkMDU0ODljOC0yZTQ5LTRiZmItOTY4Ni00MTNjYzRlYzJiNzEiLCJhdWQiOiI1bmVlaHMzYW1vc3IwcWtlOGp2dmp0Mmh1ciIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJldmVudF9pZCI6Ijg3OWQ3NGRiLTEwNjMtMTFlOC04MjZiLWZmNjc4ZGM4NTk0MyIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNTE4NDg4MDMwLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAuYXAtbm9ydGhlYXN0LTEuYW1hem9uYXdzLmNvbVwvYXAtbm9ydGhlYXN0LTFfTDR5SkJ4RU5xIiwiY29nbml0bzp1c2VybmFtZSI6ImQwNTQ4OWM4LTJlNDktNGJmYi05Njg2LTQxM2NjNGVjMmI3MSIsImV4cCI6MTUxODQ5MTYzMCwiaWF0IjoxNTE4NDg4MDMwLCJlbWFpbCI6ImFiYzEyM0B5b3BtYWlsLmNvbSJ9.Pjp8nybPUHeykqn_lPM2wl4zWUhmcuyAgr5FbsF1r5doZS6SHu2CgguKZgxljdXvin4CgHSt3NO3v216uhDlviYkCtL2-s3C1AY-4GtYsRe4NBFxrae5-myeRsQPMU5_vcS9pdpkR8rvFeVQfjKCVcjAxvAN4kUF2_TkqQDxxB1UNOTFbl7Eq6nYH-Qkeg-Gp0KGp1WVv03aK15DX28nr8HRQuPswfER_9HLSQ9DaVb-qmXjCYSy8s4BvvXtaFpPn8ZEAfjCW2C6_dV1cxC9bGdrDYqbOMudjPME4SBDXKDZ-7t7pCOSYFYKH_lNnJyHioQfhPAWXgI2Nlp9-QvUDQ")
    }
}