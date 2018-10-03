package com.user.ncard.ui.card

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentSearchFromContactBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.namecard.SearchUserContactAdapter
import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.Utils
import com.user.ncard.vo.User
import javax.inject.Inject

class SearchFromContactFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var searchUserAdapter: SearchUserContactAdapter

    lateinit var viewModel: SearchFromContactViewModel
    private lateinit var viewDataBinding: FragmentSearchFromContactBinding

    lateinit var sendingRequestAlert: AlertDialog
    lateinit var processingAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_search_from_contact, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, ContextCompat.getColor(context, R.color.colorWhite))
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).text = getString(R.string.friend_from_contact)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchFromContactViewModel::class.java)

        viewModel.userList.observe(this@SearchFromContactFragment, Observer {
            if (it?.data != null) {
                if (processingAlert.isShowing) processingAlert.cancel()
                searchUserAdapter.replace(it.data.filter { it.id != Functions.getMyId(viewModel.sharedPreferenceHelper) })
            }
        })
        searchUserAdapter = SearchUserContactAdapter(
                object : SearchUserContactAdapter.UserClickCallback {
                    override fun onClick(user: User) {
                        val profileFragment = ProfileFragment.newInstance(user)
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, profileFragment)
                                .addToBackStack(null)
                                .commitAllowingStateLoss()
                    }
                },
                object : SearchUserContactAdapter.AddFriendCallback {
                    override fun onClick(user: User) {
                        sendingRequestAlert = Utils.showAlert(activity)
                        viewModel.createFriendRequest(user)
                    }
                })
        viewDataBinding.recyclerViewCardlineFriend.apply {
            adapter = searchUserAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }

        viewModel.apply {
            sendRequestSuccess.observe(this@SearchFromContactFragment, Observer {
                sendingRequestAlert.cancel()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        processingAlert = Utils.showAlert(activity)
        object : CountDownTimer(1500.toLong(), 1000.toLong()) {
            override fun onFinish() {
                if (processingAlert.isShowing)
                    viewModel.start.value = true
            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (fragmentManager.backStackEntryCount > 0)
                    fragmentManager.popBackStack()
                else NavUtils.navigateUpFromSameTask(activity)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}