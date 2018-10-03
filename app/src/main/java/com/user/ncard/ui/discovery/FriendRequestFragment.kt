package com.user.ncard.ui.discovery

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.MutableBoolean
import android.view.*
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentFriendRequestBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.profile.ProfileFragment
import com.user.ncard.ui.chats.PrivacyListEvent
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.User
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class FriendRequestFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var friendRequestAdapter: FriendRequestAdapter
    lateinit var friendRequestSentAdapter: FriendRequestSentAdapter

    lateinit var viewModel: FriendRequestViewModel
    private lateinit var viewDataBinding: FragmentFriendRequestBinding

    lateinit var processRequestAlert: AlertDialog
    val receivedClicked = MutableBoolean(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_friend_request, container, false)!!
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
            (v.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(R.string.friend_request)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FriendRequestViewModel::class.java)

        friendRequestAdapter = FriendRequestAdapter(
                object : FriendRequestAdapter.RejectClickCallback {
                    override fun onClick(user: User) {
                        processRequestAlert = Utils.showAlert(activity)
                        viewModel.rejectFriend(user)
                    }
                },
                object : FriendRequestAdapter.AcceptClickCallback {
                    override fun onClick(user: User) {
                        processRequestAlert = Utils.showAlert(activity)
                        viewModel.acceptFriend(user)
                        // Unblock user from chat
                        // TODO: need to  wait the accept friend successfully
                        EventBus.getDefault().post(PrivacyListEvent(user.chatId.toString(), true))
                    }
                })
        friendRequestSentAdapter = FriendRequestSentAdapter(
                object : FriendRequestSentAdapter.UserClickCallback {
                    override fun onClick(user: User) {
                        val profileFragment = ProfileFragment.newInstance(user, true)
                        fragmentManager.beginTransaction()
                                .replace(R.id.container, profileFragment)
                                .addToBackStack(null)
                                .commitAllowingStateLoss()
                    }
                })
        viewDataBinding.recyclerViewFriendRequest.apply {
            adapter = friendRequestAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        viewDataBinding.recyclerViewSentRequest.apply {
            adapter = friendRequestSentAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
        }
        viewModel.apply {
            acceptSuccess.observe(this@FriendRequestFragment, Observer {
                processRequestAlert.cancel()
                start.value = true
                // Re-get all friends again
                viewModel.regetALlFriends()
            })
            rejectSuccess.observe(this@FriendRequestFragment, Observer {
                processRequestAlert.cancel()
                start.value = true
            })
        }
        viewModel.start.value = true
        viewModel.userList.observe(this@FriendRequestFragment, Observer {
            friendRequestAdapter.replace(it)
            if (it == null || it.isEmpty()) {
                viewDataBinding.recyclerViewFriendRequest.visibility = View.GONE
                viewDataBinding.textViewNoFriendRequest.visibility = View.VISIBLE
            } else {
                viewDataBinding.recyclerViewFriendRequest.visibility = View.VISIBLE
                viewDataBinding.textViewNoFriendRequest.visibility = View.GONE
            }
        })
        viewModel.sentList.observe(this@FriendRequestFragment, Observer {
            friendRequestSentAdapter.replace(it)
            if (viewDataBinding.receivedUnderline.visibility == View.INVISIBLE) {
                if (it == null || it.isEmpty()) {
                    viewDataBinding.recyclerViewSentRequest.visibility = View.GONE
                    viewDataBinding.textViewNoSentFriendRequest.visibility = View.VISIBLE
                } else {
                    viewDataBinding.recyclerViewSentRequest.visibility = View.VISIBLE
                    viewDataBinding.textViewNoSentFriendRequest.visibility = View.GONE
                }
            } else {
                viewDataBinding.recyclerViewSentRequest.visibility = View.GONE
                viewDataBinding.textViewNoSentFriendRequest.visibility = View.GONE
            }
        })
        viewDataBinding.apply {
            textViewReceived.setOnClickListener {
                receivedClicked.value = true
                showTab()
            }

            textViewSent.setOnClickListener {
                receivedClicked.value = false
                showTab()
            }
        }
        showTab()
    }

    private fun showTab() {
        if (receivedClicked.value) {
            viewDataBinding.apply {
                receivedUnderline.visibility = View.VISIBLE
                sentUnderline.visibility = View.INVISIBLE
                recyclerViewSentRequest.visibility = View.GONE
                textViewNoSentFriendRequest.visibility = View.GONE
                if (friendRequestAdapter.itemCount != 0) recyclerViewFriendRequest.visibility = View.VISIBLE
                else textViewNoFriendRequest.visibility = View.VISIBLE
            }
        } else {
            viewDataBinding.apply {
                receivedUnderline.visibility = View.INVISIBLE
                sentUnderline.visibility = View.VISIBLE
                recyclerViewFriendRequest.visibility = View.GONE
                textViewNoFriendRequest.visibility = View.GONE
                if (friendRequestSentAdapter.itemCount != 0) recyclerViewSentRequest.visibility = View.VISIBLE
                else textViewNoSentFriendRequest.visibility = View.VISIBLE
            }
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