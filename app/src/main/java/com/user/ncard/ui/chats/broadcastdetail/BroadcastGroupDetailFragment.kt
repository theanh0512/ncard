package com.user.ncard.ui.chats.broadcastdetail

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import com.barryzhang.temptyview.TViewUtil
import com.google.gson.Gson
import com.user.ncard.R
import com.user.ncard.databinding.FragmentBroadcastGroupDetailBinding
import com.user.ncard.databinding.FragmentFriendsListBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.utils.CallbackAlertDialogListener
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.broadcast.BroadcastGroupListAdapter
import com.user.ncard.ui.chats.friends.FriendsListActivity
import com.user.ncard.ui.chats.friends.FriendsListAdapter
import com.user.ncard.ui.chats.friends.FriendsListViewModel
import com.user.ncard.ui.chats.friends.FriendsSelectEvent
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SwipeToDeleteCallback
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_broadcast_group_detail.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class BroadcastGroupDetailFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        const val TYPE_VIEW = 0
        const val TYPE_CREATE = 1
        const val TYPE_UPDATE = 2
        fun newInstance(): BroadcastGroupDetailFragment = BroadcastGroupDetailFragment()
    }

    lateinit var viewModel: BroadcastGroupDetailViewModel
    lateinit var fragmentBinding: FragmentBroadcastGroupDetailBinding
    lateinit var adapter: BroadcastGroupMemberListAdapter

    @Inject
    lateinit var chatHelper: ChatHelper

    override fun getLayout(): Int {
        return R.layout.fragment_broadcast_group_detail
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_broadcast_detail), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(BroadcastGroupDetailViewModel::class.java)
        fragmentBinding = FragmentBroadcastGroupDetailBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        viewModel.init(bundle.getInt("type"), bundle.getInt("groupId"))
        // viewModel.initData()
    }

    override fun init() {
        // Load data here
        initObser()
        initData()
        initAdapter()
        iniSwipeRefreshLayout()

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
            viewModel.groupId.value = viewModel.id
        }

        fragmentBinding.viewAddNewMember.setOnClickListener(View.OnClickListener {
            startActivity(FriendsListActivity.getIntent(activity,
                    FriendsListActivity.BROADCAST_GROUP_DETAIL,
                    if (viewModel.getBroadcastMember() != null) Gson().toJson(viewModel.getBroadcastMember()) else null, null))
        })

        fragmentBinding.viewDeleteGroupName.setOnClickListener(View.OnClickListener {
            Functions.showAlertDialogYesNo(activity, "", "Do you want to leave this group?", object : CallbackAlertDialogListener {
                override fun onClickOk() {
                    viewModel.delete.value = true
                }

                override fun onClickCancel() {
                }
            })
        })

        EventBus.getDefault().register(this)
    }

    fun initValue() {
        fragmentBinding.edtGroupName.setText(viewModel?.item?.value?.data?.name)
    }

    fun initData() {
        when (viewModel.type) {
            TYPE_CREATE -> {
                fragmentBinding.viewDeleteGroupName.visibility = View.GONE
            }
            TYPE_UPDATE -> {
                fragmentBinding.viewDeleteGroupName.visibility = View.VISIBLE
            }
            TYPE_VIEW -> {
                fragmentBinding.viewDeleteGroupName.visibility = View.GONE
            }
        }
        if (viewModel.getItems()?.size > 0) {
            fragmentBinding?.viewSwipeToDelete.visibility = View.VISIBLE
            fragmentBinding?.viewFriends.visibility = View.VISIBLE
        } else {
            fragmentBinding?.viewSwipeToDelete.visibility = View.GONE
            fragmentBinding?.viewFriends.visibility = View.GONE
        }
    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = BroadcastGroupMemberListAdapter(activity, viewModel.getItems())

        adapter.setLoadingOffset(1)
        recyclerview.adapter = adapter

        val swipeHandler = object : SwipeToDeleteCallback(activity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                viewModel.removeMemberAt(viewHolder?.adapterPosition!!)
                notifyAdapterChange()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerview)
    }

    fun iniSwipeRefreshLayout() {
        fragmentBinding.srl.setColorSchemeResources(R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue)
        fragmentBinding.srl.isEnabled = false
        fragmentBinding.srl.setOnRefreshListener(this)
    }

    fun initObser() {
        // listen load broadcast group
        viewModel.item.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                if (fragmentBinding.srl.isRefreshing != true) {
                    showProgressDialog()
                }
            } else if (it?.status == Status.SUCCESS) {
                loadingFinish()
                initValue()
                notifyAdapterChange()
            } else if (it?.status == Status.ERROR) {
                loadingFinish()
                if (it?.data?.members?.isNotEmpty()!!) {
                    initValue()
                    notifyAdapterChange()
                }
                showSnackbarMessage(it?.message)
            } else {
                loadingFinish()
            }
        })
        // listen create broadcast group
        viewModel.broadcastGroupCreate.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                if (it?.data != null && viewModel.type == TYPE_CREATE) {
                    viewModel.id = it?.data.id!!
                    viewModel.type = TYPE_UPDATE
                    // This broacastgroup doesn't have members, reload data again
                    viewModel.refresh()
                }
            } else if (it?.status == Status.ERROR) {
                showSnackbarMessage(it?.message)
            } else {
                loadingFinish()
            }
        })

        // Listen delete broadcast group
        viewModel.deleteItem.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                Toast.makeText(activity, it.data?.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
                activity.finish()
            } else if (it?.status == Status.ERROR) {
                Toast.makeText(activity, it?.message, Toast.LENGTH_LONG).show()
                showSnackbarMessage(it?.message)
            } else {
                loadingFinish()
            }
        })
        // Listen Update broadcast group
        viewModel.updateItem.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                Toast.makeText(activity, it.data?.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
                // This is only message, reload data again
                viewModel.refresh()
            } else if (it?.status == Status.ERROR) {
                Toast.makeText(activity, it?.message, Toast.LENGTH_LONG).show()
                showSnackbarMessage(it?.message)
            } else {
                loadingFinish()
            }
        })
    }

    fun loadingFinish() {
        viewModel.forceLoad = false
        viewModel.isLoading = false
        viewModel.refresh = false;
        fragmentBinding.srl.isRefreshing = false
        hideProgressDialog()
    }

    fun notifyAdapterChange() {
        adapter.updateItems(viewModel.getItems())
        adapter.notifyDataSetChanged()
        /*TViewUtil.EmptyViewBuilder.getInstance(activity)
                .bindView(recyclerview)*/
        initData()
        activity.invalidateOptionsMenu()
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onFriendsSelectEvent(friendsSelectEvent: FriendsSelectEvent) {
        if (friendsSelectEvent.from != FriendsListActivity.BROADCAST_GROUP_DETAIL) {
            return
        }
        if (friendsSelectEvent?.list != null && friendsSelectEvent?.list.isNotEmpty()) {
            viewModel.listFriends = ArrayList<Friend>(friendsSelectEvent?.list)
            viewModel.removeAllMembersBroadcastGroup()
            notifyAdapterChange()
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val itemDoSth = menu?.findItem(R.id.menu_item_do)
        if (viewModel.type == TYPE_CREATE) {
            itemDoSth?.title = activity.getString(R.string.create)
        } else if (viewModel.type == TYPE_UPDATE) {
            itemDoSth?.title = activity.getString(R.string.update)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_do_sth, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_do -> {
                consume {
                    if (fragmentBinding.edtGroupName.text.toString()?.isNotEmpty()) {
                        val listMemberIds = viewModel.getItems()?.map { it.id }
                        viewModel.createOrUpadateBroadcastGroup(
                                BroadcastGroup(viewModel.id, fragmentBinding.edtGroupName.text.toString(), listMemberIds, null))
                    } else {
                        Functions.showToastShortMessage(activity, "Please add group name")
                    }
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}