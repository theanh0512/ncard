package com.user.ncard.ui.me.ewallet

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChooseFriendAndCardBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.group.SelectFriendAdapter
import com.user.ncard.ui.group.SelectGroupItemViewModel
import com.user.ncard.util.Utils
import com.user.ncard.vo.Friend
import javax.inject.Inject

class SelectFriendFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentChooseFriendAndCardBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var friendAdapter: SelectFriendAdapter
    lateinit var viewModel: SelectGroupItemViewModel
    val friendsList = ArrayList<Friend>()
    val fullList = ArrayList<Friend>()

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
                val returnIntent = Intent()
                returnIntent.putExtra("friend", friend)
                activity.setResult(Activity.RESULT_OK, returnIntent)
                activity.finish()
            }
        })
        viewDataBinding.recyclerViewCardlineFriend.apply {
            isNestedScrollingEnabled = false
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        viewModel.userList.observe(this@SelectFriendFragment, Observer<List<Friend>> { friendList ->
            friendsList.clear()
            friendList?.let {
                friendsList.addAll(it)
            }
            fullList.addAll(friendsList)
            friendAdapter.replace(friendsList)
            friendAdapter.notifyDataSetChanged()
        })
        viewDataBinding.apply {
            recyclerViewNameCards.visibility = View.GONE
            textViewNameCard.visibility = View.GONE
        }
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

            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun newInstance() = SelectFriendFragment().apply {
        }

    }
}