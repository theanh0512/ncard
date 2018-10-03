package com.user.ncard.ui.chats.forward

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import com.user.ncard.R
import com.user.ncard.databinding.FragmentForwardListBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.friends.FriendsListAdapter
import com.user.ncard.util.ChatHelper
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_forward_list.*
import kotlinx.android.synthetic.main.view_search.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ForwardListFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance(): ForwardListFragment = ForwardListFragment()
    }

    lateinit var viewModel: ForwardListViewModel
    lateinit var fragmentBinding: FragmentForwardListBinding
    lateinit var adapter: FriendsListAdapter
    lateinit var adapterDialog: DialogsGroupListAdapter

    @Inject
    lateinit var chatHelper: ChatHelper

    var from: Int = -1

    override fun getLayout(): Int {
        return R.layout.fragment_forward_list
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_select_friends_groups), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ForwardListViewModel::class.java)
        fragmentBinding = FragmentForwardListBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        from = bundle.getInt("from")
        viewModel.initData(bundle.getInt("chatUserId"), bundle.getString("groupDialogId"))
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

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.getFilter()?.filter(newText)
                adapterDialog.getFilter()?.filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                Functions.hideSoftKeyboard(activity)
                return false
            }
        })
    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = FriendsListAdapter(activity, viewModel.getItems(), viewModel.itemsSelected,
                viewModel.friendsSelected, viewModel.occupants, from)

        recyclerViewGroupDialog.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerViewGroupDialog.setHasFixedSize(true)
        adapterDialog = DialogsGroupListAdapter(activity, viewModel.getChatDialogGroup(), viewModel.groupDialogsSelected)

        adapter.setLoadingOffset(1)
        adapterDialog.setLoadingOffset(1)
        recyclerview.adapter = adapter
        recyclerViewGroupDialog.adapter = adapterDialog

        ViewCompat.setNestedScrollingEnabled(recyclerview, false)
        ViewCompat.setNestedScrollingEnabled(recyclerViewGroupDialog, false)
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

        viewModel.lsGroupDialogs.observe(this, Observer {
            if (it != null) {
                notifyAdapterChange()
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
        adapterDialog.updateItems(viewModel.getChatDialogGroup())
        adapterDialog.notifyDataSetChanged()
        /*TViewUtil.EmptyViewBuilder.getInstance(activity)
                .bindView(recyclerview)*/
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val itemDoSth = menu?.findItem(R.id.menu_item_do)
        itemDoSth?.title = activity.getString(R.string.done)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_do_sth, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_do -> {
                consume {
                    if (viewModel.itemsSelected?.size() > 0 || viewModel.groupDialogsSelected?.size > 0) {
                        EventBus.getDefault().post(ForwardSelectEvent(Functions.sparrArrToArr(viewModel.itemsSelected), Functions.arrayMapToArr(viewModel.groupDialogsSelected)))
                        activity.finish()
                    } else {
                        Functions.showToastShortMessage(activity, "Please select at least 1 friend or group")
                    }
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}