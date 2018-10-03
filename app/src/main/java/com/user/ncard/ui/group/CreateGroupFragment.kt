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
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCreateGroupBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.FriendAdapter
import com.user.ncard.ui.card.NameCardAdapter
import com.user.ncard.ui.card.namecard.NameCardDetailActivity
import com.user.ncard.ui.card.profile.FriendProfileActivity
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.discovery.DiscoveryNavigation
import com.user.ncard.util.SwipeToDeleteCallback
import com.user.ncard.util.Utils
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Group
import com.user.ncard.vo.GroupItem
import com.user.ncard.vo.NameCard
import javax.inject.Inject

class CreateGroupFragment : Fragment(), Injectable {

    private lateinit var viewDataBinding: FragmentCreateGroupBinding
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    var group: Group? = null

    lateinit var viewModel: CreateGroupViewModel
    val friendsList = ArrayList<Friend>()
    val nameCardsList = ArrayList<NameCard>()
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var nameCardAdapter: NameCardAdapter
    lateinit var createAlert: AlertDialog
    var menuItemCreate: MenuItem? = null
    var menuItemUpdate: MenuItem? = null
    var groupId = 1
    lateinit var actionBarView: View
    lateinit var deleteAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun updateNameCardList(nameCards: List<NameCard>) {
        nameCardsList.clear()
        nameCardsList.addAll(nameCards)
        nameCardAdapter.replace(nameCardsList)
        nameCardAdapter.notifyDataSetChanged()
    }

    private fun showUpdateAndDelete() {
        menuItemCreate?.isVisible = false
        menuItemUpdate?.isVisible = true
        viewDataBinding.textViewDeleteGroup.visibility = View.VISIBLE
    }

    private fun initAdapter() {
        friendAdapter = FriendAdapter(object : FriendAdapter.FriendClickCallback {
            override fun onClick(user: Friend) {
                val intent = Intent(activity, FriendProfileActivity::class.java)
                intent.putExtra("user", user)
                startActivity(intent)
            }
        })
        nameCardAdapter = NameCardAdapter(object : NameCardAdapter.NameCardClickCallback {
            override fun onClick(nameCard: NameCard) {
                val intent = Intent(activity, NameCardDetailActivity::class.java)
                intent.putExtra("namecard", nameCard)
                startActivity(intent)
            }
        })
    }

    private fun initRecyclerView() {
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

        val swipeHandler = object : SwipeToDeleteCallback(activity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = viewDataBinding.recyclerViewCardlineFriend.adapter as FriendAdapter
                friendsList.removeAt(viewHolder.adapterPosition)
                adapter.replace(friendsList)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                setHeadlineVisibility()

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(viewDataBinding.recyclerViewCardlineFriend)

        val swipeHandlerNameCard = object : SwipeToDeleteCallback(activity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = viewDataBinding.recyclerViewNameCards.adapter as NameCardAdapter
                nameCardsList.removeAt(viewHolder.adapterPosition)
                adapter.replace(nameCardsList)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                setHeadlineVisibility()
            }
        }
        val itemTouchHelperNameCard = ItemTouchHelper(swipeHandlerNameCard)
        itemTouchHelperNameCard.attachToRecyclerView(viewDataBinding.recyclerViewNameCards)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_create_group, container, false)!!
        setUpActionBar(activity, viewDataBinding.toolbar, R.string.create_group, R.color.colorWhite)
        viewDataBinding.addMember = object : DiscoveryNavigation(getString(R.string.add_new_member), 0) {
            override fun onClick(view: View) {
                val intent = Intent(activity, SelectGroupItemActivity::class.java)
                intent.putParcelableArrayListExtra("namecard", nameCardsList)
                intent.putParcelableArrayListExtra("friend", friendsList)
                startActivityForResult(intent, REQUEST_ITEM)
            }
        }

        return viewDataBinding.root
    }

    private fun setUpActionBar(activity: Activity, toolbar: Toolbar, title: Int, color: Int = R.color.colorDarkerWhite) {
        Utils.setWindowsWithBackgroundColor(activity, ContextCompat.getColor(activity, color))
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            val inflaterActionbar = LayoutInflater.from(activity)
            actionBarView = inflaterActionbar.inflate(R.layout.action_bar_layout, null)
            val params = ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER)
            actionBar.setCustomView(actionBarView, params)
            (actionBarView.findViewById<View>(R.id.text_view_action_bar) as TextView).setText(title)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.back)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateGroupViewModel::class.java)
        initAdapter()
        initRecyclerView()

        group = arguments.getParcelable(ARGUMENT_GROUP)
        if (group != null) {
            viewDataBinding.editTextGroupName.setText(group?.name)
            (actionBarView.findViewById<View>(R.id.text_view_action_bar) as TextView).text = group?.name
            viewModel.getNameCardAndFriendForGroup(group!!)
            groupId = group?.id ?: 1
        }

        viewModel.createGroupSuccess.observe(this@CreateGroupFragment, Observer {
            createAlert.cancel()
            showUpdateAndDelete()
        })
        viewModel.groupLiveData.observe(this@CreateGroupFragment, Observer<Group> { group ->
            if (group != null) {
                groupId = group.id
                (actionBarView.findViewById<View>(R.id.text_view_action_bar) as TextView).text = group.name
            }
        })

        viewModel.friendList.observe(this@CreateGroupFragment, Observer<List<Friend>> { friends ->
            if (friends != null) {
                updateFriendList(friends)
                setHeadlineVisibility()
            }
        })
        viewModel.nameCardList.observe(this@CreateGroupFragment, Observer<List<NameCard>> { nameCards ->
            if (nameCards != null) {
                updateNameCardList(nameCards)
                setHeadlineVisibility()
            }
        })

        viewDataBinding.textViewDeleteGroup.setOnClickListener {
            deleteAlert = Utils.showAlert(activity)
            viewModel.deleteGroup(groupId)
        }
        viewModel.deleteGroupSuccess.observe(this@CreateGroupFragment, Observer {
            deleteAlert.cancel()
            activity.onBackPressed()
        })
    }

    private fun updateFriendList(friends: List<Friend>) {
        friendsList.clear()
        friendsList.addAll(friends)
        friendAdapter.replace(friendsList)
        friendAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                updateFriendList(data?.getParcelableArrayListExtra("friend") ?: ArrayList())
                updateNameCardList(data?.getParcelableArrayListExtra("namecard") ?: ArrayList())

                setHeadlineVisibility()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setHeadlineVisibility() {
        viewDataBinding.apply {
            textViewNameCard.visibility = if (nameCardsList.isEmpty()) View.INVISIBLE else View.VISIBLE
            textViewCardlineFriends.visibility = if (friendsList.isEmpty()) View.INVISIBLE else View.VISIBLE
            textViewSwipeToDelete.visibility = if (friendsList.isEmpty() && nameCardsList.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_create, menu)
        menuItemCreate = menu?.findItem(R.id.menu_item_create)
        menuItemUpdate = menu?.findItem(R.id.menu_item_update)
        if (group != null) {
            showUpdateAndDelete()
        } else hideUpdateAndDelete()
        setHeadlineVisibility()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun hideUpdateAndDelete() {
        menuItemCreate?.isVisible = true
        menuItemUpdate?.isVisible = false
        viewDataBinding.textViewDeleteGroup.visibility = View.INVISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (fragmentManager.backStackEntryCount > 0)
                    fragmentManager.popBackStack()
                else NavUtils.navigateUpFromSameTask(activity)
                return true
            }
            R.id.menu_item_create -> {
                if (!viewDataBinding.editTextGroupName.text.isNullOrEmpty()) {
                    val groupToCreate = prepareGroupData()
                    viewModel.createGroup(groupToCreate)
                } else {
                    Functions.showToastShortMessage(activity, getString(R.string.warn_add_group_name))
                    viewDataBinding.editTextGroupName.requestFocus()
                }
            }
            R.id.menu_item_update -> {
                if (!viewDataBinding.editTextGroupName.text.isNullOrEmpty()) {
                    val groupToUpdate = prepareGroupData()
                    (actionBarView.findViewById<View>(R.id.text_view_action_bar) as TextView).text = groupToUpdate.name
                    viewModel.updateGroup(groupToUpdate)
                } else {
                    Functions.showToastShortMessage(activity, getString(R.string.warn_add_group_name))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun prepareGroupData(): Group {
        createAlert = Utils.showAlert(activity)
        val members = friendsList.map { friend -> GroupItem(friend.id) }
        val nameCards = nameCardsList.map { nameCard -> GroupItem(nameCard.id) }
        val groupName = viewDataBinding.editTextGroupName.text?.toString() ?: ""
        val groupToCreate = Group(groupId, groupName, members, nameCards)
        return groupToCreate
    }

    companion object {
        private const val REQUEST_ITEM = 101
        private const val ARGUMENT_GROUP = "GROUP"

        fun newInstance(group: Group?) = CreateGroupFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_GROUP, group)
            }
        }
    }
}