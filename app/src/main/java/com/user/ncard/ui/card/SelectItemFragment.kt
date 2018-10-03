package com.user.ncard.ui.card

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChooseFriendAndCardBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.group.SelectFriendAdapter
import com.user.ncard.ui.group.SelectGroupItemViewModel
import com.user.ncard.util.Utils
import com.user.ncard.vo.BaseEntity
import com.user.ncard.vo.Friend
import com.user.ncard.vo.NameCard
import javax.inject.Inject

class SelectItemFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentChooseFriendAndCardBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var friendAdapter: SelectFriendAdapter
    lateinit var viewModel: SelectGroupItemViewModel
    val friendsList = ArrayList<Friend>()
    val fullList = ArrayList<Friend>()
    lateinit var shareAlert: AlertDialog
    lateinit var resultAlert: AlertDialog
    lateinit var noFriendsChosen: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SelectGroupItemViewModel::class.java)
        viewModel.getFriendsAndNameCard()
        friendAdapter = SelectFriendAdapter(object : SelectFriendAdapter.OnClickCallback {
            override fun onClick(friend: Friend) {
                friend.isChecked = friend.isChecked == null || friend.isChecked == false
            }
        })
        viewDataBinding.searchView.setOnQueryChangeListener { oldQuery, newQuery ->
            val filteredList = fullList.filter {
                it.firstName?.contains(newQuery) ?: false || it.lastName?.contains(newQuery) ?: false ||
                        it.email?.contains(newQuery) ?: false
            }
            friendsList.clear()
            friendsList.addAll(filteredList)
            friendAdapter.replace(friendsList)
            friendAdapter.notifyDataSetChanged()
        }
        viewDataBinding.recyclerViewCardlineFriend.apply {
            isNestedScrollingEnabled = false
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        val friendToBeShared = arguments.getParcelable<BaseEntity>(ARGUMENT_FRIEND)
        val nameCardToBeShared = arguments.getParcelable<NameCard>(ARGUMENT_NAME_CARD)
        viewModel.userList.observe(this@SelectItemFragment, Observer<List<Friend>> { friendList ->
            friendsList.clear()
            friendList?.let {
                friendsList.addAll(it.filter { it.id != friendToBeShared?.id })
            }
            fullList.addAll(friendsList)
            friendAdapter.replace(friendsList)
            friendAdapter.notifyDataSetChanged()
        })
        viewDataBinding.apply {
            recyclerViewNameCards.visibility = View.GONE
            textViewNameCard.visibility = View.GONE
        }
        viewModel.successShareEvent.observe(this@SelectItemFragment, Observer {
            shareAlert.cancel()
            resultAlert = if (friendToBeShared != null)
                Utils.showAlertWithCheckIcon(activity, getString(R.string.display_sent_sharing, friendToBeShared.firstName))
            else Utils.showAlertWithCheckIcon(activity, getString(R.string.display_sent_sharing, nameCardToBeShared.name))
            object : CountDownTimer(2000.toLong(), 1000.toLong()) {
                override fun onFinish() {
                    resultAlert.cancel()
                    activity.onBackPressed()
                }

                override fun onTick(millisUntilFinished: Long) {
                }

            }.start()
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_choose_friend_and_card, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.title_select_friends)

        return viewDataBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_done, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
            R.id.menu_item_done -> {
                val friendToBeShared = arguments.getParcelable<BaseEntity>(ARGUMENT_FRIEND)
                val nameCard = arguments.getParcelable<NameCard>(ARGUMENT_NAME_CARD)
                val chosenFriends = friendsList.filter { friend -> !(friend.isChecked == null || !friend.isChecked!!) }.map { friend -> friend.id }
                if (chosenFriends.isNotEmpty()) {
                    shareAlert = Utils.showAlert(activity)
                    if (friendToBeShared != null) viewModel.shareFriend(friendToBeShared, chosenFriends as ArrayList)
                    else viewModel.shareNameCard(nameCard, chosenFriends as ArrayList)
                } else {
                    noFriendsChosen = Utils.showAlert(activity, getString(R.string.choose_at_least_1_to_share))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_FRIEND = "FRIEND"
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"

        fun newInstance(friend: BaseEntity?, nameCard: NameCard?) = SelectItemFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_FRIEND, friend)
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
            }
        }

    }
}