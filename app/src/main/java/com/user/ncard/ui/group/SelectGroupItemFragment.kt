package com.user.ncard.ui.group

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChooseFriendAndCardBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.Utils
import com.user.ncard.vo.Friend
import com.user.ncard.vo.NameCard
import javax.inject.Inject

class SelectGroupItemFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentChooseFriendAndCardBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var friendAdapter: SelectFriendAdapter
    private lateinit var nameCardAdapter: SelectNameCardAdapter
    lateinit var viewModel: SelectGroupItemViewModel
    val friendsList = ArrayList<Friend>()
    val nameCardsList = ArrayList<NameCard>()
    val fullListFriend = ArrayList<Friend>()
    val fullListNameCard = ArrayList<NameCard>()

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

        val selectedFriends = arguments.getParcelableArrayList<Friend>(ARGUMENT_FRIEND)
        val selectedNameCards = arguments.getParcelableArrayList<NameCard>(ARGUMENT_NAME_CARD)

        nameCardAdapter = SelectNameCardAdapter(object : SelectNameCardAdapter.OnClickCallback {
            override fun onClick(nameCard: NameCard) {
                nameCard.isChecked = nameCard.isChecked == null || nameCard.isChecked == false
            }
        })
        viewDataBinding.recyclerViewCardlineFriend.apply {
            isNestedScrollingEnabled = false
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(this.getContext(),
                    (layoutManager as LinearLayoutManager).getOrientation());
            dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.view_divider_decoration));
            addItemDecoration(dividerItemDecoration)
        }
        viewDataBinding.recyclerViewNameCards.apply {
            isNestedScrollingEnabled = false
            adapter = nameCardAdapter
            layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration = DividerItemDecoration(this.getContext(),
                    (layoutManager as LinearLayoutManager).getOrientation());
            dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.view_divider_decoration));
            addItemDecoration(dividerItemDecoration)
        }
        viewModel.userList.observe(this@SelectGroupItemFragment, Observer<List<Friend>> { friendList ->
            friendsList.clear()
            friendList?.let {
                friendsList.addAll(it)
                selectedFriends.forEach { selectedFriend ->
                    friendsList.forEach {
                        if (selectedFriend.id == it.id) it.isChecked = true
                    }
                }
            }
            fullListFriend.addAll(friendsList)
            friendAdapter.replace(friendsList)
            friendAdapter.notifyDataSetChanged()
        })
        viewModel.nameCardList.observe(this@SelectGroupItemFragment, Observer<List<NameCard>> { nameCardList ->
            nameCardsList.clear()
            nameCardList?.let {
                nameCardsList.addAll(it)
                selectedNameCards.forEach { selectedNameCard ->
                    nameCardsList.forEach {
                        if (selectedNameCard.id == it.id) it.isChecked = true
                    }
                }
            }
            fullListNameCard.addAll(nameCardsList)
            nameCardAdapter.replace(nameCardsList)
            nameCardAdapter.notifyDataSetChanged()
        })
        //Search
        viewDataBinding.searchView.setOnQueryChangeListener { oldQuery, newQuery ->
            val filteredList = fullListFriend.filter {
                it.firstName?.toLowerCase()?.contains(newQuery.toLowerCase()) ?: false || it.lastName?.toLowerCase()?.contains(newQuery.toLowerCase()) ?: false ||
                        it.email?.toLowerCase()?.contains(newQuery.toLowerCase()) ?: false
            }
            friendsList.clear()
            friendsList.addAll(filteredList)
            friendAdapter.replace(friendsList)
            friendAdapter.notifyDataSetChanged()

            val filteredNameCardList = fullListNameCard.filter {
                it.name?.toLowerCase()?.contains(newQuery.toLowerCase()) ?: false || it.email?.toLowerCase()?.contains(newQuery.toLowerCase()) ?: false
            }
            nameCardsList.clear()
            nameCardsList.addAll(filteredNameCardList)
            nameCardAdapter.replace(nameCardsList)
            nameCardAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_choose_friend_and_card, container, false)!!
        Utils.setUpActionBar(activity, viewDataBinding.toolbar, R.string.select_contact_cards)

        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_done, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (fragmentManager.backStackEntryCount > 0)
                    fragmentManager.popBackStack()
                else NavUtils.navigateUpFromSameTask(activity)
                return true
            }
            R.id.menu_item_done -> {
                val returnIntent = Intent()
                val chosenFriends = friendsList.filter { friend -> !(friend.isChecked == null || !friend.isChecked!!) }
                val chosenNameCards = nameCardsList.filter { nameCard -> !(nameCard.isChecked == null || !nameCard.isChecked!!) }
                returnIntent.putParcelableArrayListExtra("namecard", chosenNameCards as ArrayList)
                returnIntent.putParcelableArrayListExtra("friend", chosenFriends as ArrayList)
                activity.setResult(Activity.RESULT_OK, returnIntent)
                activity.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"
        private const val ARGUMENT_FRIEND = "FRIEND"

        fun newInstance(friends: ArrayList<Friend>, nameCards: ArrayList<NameCard>) = SelectGroupItemFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARGUMENT_NAME_CARD, nameCards)
                putParcelableArrayList(ARGUMENT_FRIEND, friends)
            }
        }

    }
}