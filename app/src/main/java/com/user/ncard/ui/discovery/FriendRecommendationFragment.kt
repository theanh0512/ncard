package com.user.ncard.ui.discovery

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentRecommendationBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.SwipeToDeleteCallback
import com.user.ncard.util.Utils
import com.user.ncard.vo.FriendRecommendation
import com.user.ncard.vo.NameCardRecommendation
import javax.inject.Inject


class FriendRecommendationFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: FriendRecommendationViewModel
    private lateinit var viewDataBinding: FragmentRecommendationBinding

    private lateinit var friendAdapter: RecommendedUserAdapter
    private lateinit var nameCardAdapter: RecommendedNameCardAdapter
    var sendingRequestAlert: AlertDialog? = null
    var progressingAlert: AlertDialog? = null
    val friendRecommendationList = ArrayList<FriendRecommendation>()
    val nameCardRecommendationList = ArrayList<NameCardRecommendation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_recommendation, container, false)!!

        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.recommendation)

        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FriendRecommendationViewModel::class.java)
        friendAdapter = RecommendedUserAdapter(
                object : RecommendedUserAdapter.UserClickCallback {
                    override fun onClick(friendRecommendation: FriendRecommendation) {
                    }
                },
                object : RecommendedUserAdapter.AddFriendCallback {
                    override fun onClick(friendRecommendation: FriendRecommendation) {
                        //sendingRequestAlert = Utils.showAlertWithCheckIcon(activity, R.string.accept_success)
                        //click add means accept the friend sharing. Delete it means reject
                        viewModel.updateFriendSharing(friendRecommendation, "accept")
                    }
                })
        nameCardAdapter = RecommendedNameCardAdapter(
                object : RecommendedNameCardAdapter.UserClickCallback {
                    override fun onClick(friendRecommendation: NameCardRecommendation) {
                    }
                },
                object : RecommendedNameCardAdapter.AddNameCardCallback {
                    override fun onClick(friendRecommendation: NameCardRecommendation) {
                        //sendingRequestAlert = Utils.showAlertWithCheckIcon(activity, R.string.accept_success)
                        //click add means accept the name card sharing. Delete it means reject
                        viewModel.updateNameCardSharing(friendRecommendation, "accept")
                    }
                })
        viewDataBinding.recyclerViewCardlineFriend.apply {
            isNestedScrollingEnabled = false
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        viewDataBinding.recyclerViewNameCards.apply {
            isNestedScrollingEnabled = false
            adapter = nameCardAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        val swipeHandler = object : SwipeToDeleteCallback(activity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (progressingAlert == null) progressingAlert = Utils.showAlert(activity)
                else progressingAlert?.show()
                val adapter = viewDataBinding.recyclerViewCardlineFriend.adapter as RecommendedUserAdapter
                viewModel.updateFriendSharing(friendRecommendationList[viewHolder.adapterPosition], "reject")
                friendRecommendationList.removeAt(viewHolder.adapterPosition)
                adapter.replace(friendRecommendationList)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(viewDataBinding.recyclerViewCardlineFriend)

        val swipeHandlerNameCard = object : SwipeToDeleteCallback(activity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (progressingAlert == null) progressingAlert = Utils.showAlert(activity)
                else progressingAlert?.show()
                val adapter = viewDataBinding.recyclerViewNameCards.adapter as RecommendedNameCardAdapter
                viewModel.updateNameCardSharing(nameCardRecommendationList[viewHolder.adapterPosition], "reject")
                nameCardRecommendationList.removeAt(viewHolder.adapterPosition)
                adapter.replace(nameCardRecommendationList)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelperNameCard = ItemTouchHelper(swipeHandlerNameCard)
        itemTouchHelperNameCard.attachToRecyclerView(viewDataBinding.recyclerViewNameCards)

        viewModel.start.value = true
        setupUserList()
        viewModel.successEvent.observe(this@FriendRecommendationFragment, Observer {
            if (progressingAlert?.isShowing == true) progressingAlert!!.cancel()
            //if (sendingRequestAlert?.isShowing == true) sendingRequestAlert!!.cancel()
            Functions.showToastShortMessage(activity, getString(R.string.accept_success))
            viewModel.start.value = true
        })
    }

    private fun setupUserList() {
        viewModel.userList.observe(this@FriendRecommendationFragment, Observer { friendList ->
            if (friendList != null) {
                friendRecommendationList.apply {
                    clear()
                    addAll(friendList)
                }
            } else friendRecommendationList.clear()
            friendAdapter.replace(friendRecommendationList)
            friendAdapter.notifyDataSetChanged()
            showHideNoFriendRecommendationText()
        })
        viewModel.nameCardList.observe(this@FriendRecommendationFragment, Observer { nameCardList ->
            if (nameCardList != null) {
                nameCardRecommendationList.apply {
                    clear()
                    addAll(nameCardList)
                }
            } else nameCardRecommendationList.clear()
            nameCardAdapter.replace(nameCardRecommendationList)
            nameCardAdapter.notifyDataSetChanged()
            showHideNoFriendRecommendationText()
        })
    }

    private fun showHideNoFriendRecommendationText() {
        if (friendRecommendationList.isEmpty() && nameCardRecommendationList.isEmpty()) {
            viewDataBinding.apply {
                textViewFriendRecommendation.visibility = View.GONE
                textViewNameCardRecommendation.visibility = View.GONE
                textViewNoFriendRecommendation.visibility = View.VISIBLE
                recyclerViewCardlineFriend.visibility = View.GONE
                recyclerViewNameCards.visibility = View.GONE
            }
        } else {
            viewDataBinding.apply {
                textViewFriendRecommendation.visibility = View.VISIBLE
                textViewNameCardRecommendation.visibility = View.VISIBLE
                textViewNoFriendRecommendation.visibility = View.GONE
                recyclerViewCardlineFriend.visibility = View.VISIBLE
                recyclerViewNameCards.visibility = View.VISIBLE
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