package com.user.ncard.ui.chats.broadcast

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import com.barryzhang.temptyview.TViewUtil
import com.user.ncard.R
import com.user.ncard.databinding.FragmentBroadcastGroupListBinding
import com.user.ncard.databinding.FragmentFriendsListBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.broadcastchat.BroadcastChatActivity
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailActivity
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailFragment
import com.user.ncard.ui.chats.friends.FriendsListAdapter
import com.user.ncard.ui.chats.friends.FriendsListViewModel
import com.user.ncard.ui.chats.friends.FriendsSelectEvent
import com.user.ncard.util.ChatHelper
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_broadcast_group_list.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class BroacastGroupListFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, BroadcastGroupListAdapter.OnClickListener {

    companion object {
        fun newInstance(): BroacastGroupListFragment = BroacastGroupListFragment()
    }

    lateinit var viewModel: BroadcastGroupListViewModel
    lateinit var fragmentBinding: FragmentBroadcastGroupListBinding
    lateinit var adapter: BroadcastGroupListAdapter

    @Inject
    lateinit var chatHelper: ChatHelper

    override fun getLayout(): Int {
        return R.layout.fragment_broadcast_group_list
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_broadcast), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(BroadcastGroupListViewModel::class.java)
        fragmentBinding = FragmentBroadcastGroupListBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        // viewModel.initData()
    }

    override fun init() {
        // Load data here
        initObser()
        initAdapter()
        iniSwipeRefreshLayout()

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
            viewModel.start.value = true
        }
    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = BroadcastGroupListAdapter(activity, viewModel.getItems(), this)

        adapter.setLoadingOffset(1)
        recyclerview.adapter = adapter
    }

    fun iniSwipeRefreshLayout() {
        fragmentBinding.srl.setColorSchemeResources(R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue)
        fragmentBinding.srl.isEnabled = true
        fragmentBinding.srl.setOnRefreshListener(this)
    }

    fun initObser() {
        viewModel.items.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
            } else if (it?.status == Status.SUCCESS) {
                if (it?.pagination != null) {
                    viewModel.pagination = it?.pagination
                }
                loadingFinish()
                adapter.onNextItemsLoaded()
                notifyAdapterChange()
            } else if (it?.status == Status.ERROR) {
                loadingFinish()
                if (viewModel.page > viewModel.DEFAULT_PAGE) {
                    viewModel.page--
                }
                adapter.onNextItemsError()
                if (it?.data?.isNotEmpty()!!) {
                    notifyAdapterChange()
                }
                showSnackbarMessage(it?.message)
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
        TViewUtil.EmptyViewBuilder.getInstance(activity)
                .bindView(recyclerview)
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    override fun onImageDetailClick(item: BroadcastGroup, position: Int) {
        startActivity(BroadcastGroupDetailActivity.getIntent(activity, BroadcastGroupDetailFragment.TYPE_UPDATE, item.id))
    }

    override fun onItemClick(item: BroadcastGroup, position: Int) {
        if(item.memberIds != null && item.memberIds?.size > 0) {
            startActivity(BroadcastChatActivity.getIntent(activity, item))
        } else {
            Functions.showToastShortMessage(activity, getString(R.string.warn_broadcast_group_member))
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val itemDoSth = menu?.findItem(R.id.menu_item_do)
        itemDoSth?.title = activity.getString(R.string.add)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_do_sth, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_do -> {
                consume {
                    startActivity(BroadcastGroupDetailActivity.getIntent(activity, BroadcastGroupDetailFragment.TYPE_CREATE, -1))
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}