package com.user.ncard.ui.chats.friends

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.SearchView
import com.barryzhang.temptyview.TViewUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.user.ncard.R
import com.user.ncard.databinding.FragmentFriendsListBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.ChatHelper
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_friends_list.*
import kotlinx.android.synthetic.main.view_search.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class FriendsListFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance(): FriendsListFragment = FriendsListFragment()
    }

    lateinit var viewModel: FriendsListViewModel
    lateinit var fragmentBinding: FragmentFriendsListBinding
    lateinit var adapter: FriendsListAdapter

    @Inject
    lateinit var chatHelper: ChatHelper

    var from: Int = -1

    override fun getLayout(): Int {
        return R.layout.fragment_friends_list
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_select_friends), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FriendsListViewModel::class.java)
        fragmentBinding = FragmentFriendsListBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        from = bundle.getInt("from")
        val strFriends = bundle.getString("friendsSelected")
        val strOccupants = bundle.getString("occupants")
        if (strFriends != null && strFriends.isNotBlank()) {
            viewModel.init(Gson().fromJson(
                    strFriends,
                    object : TypeToken<List<Int>>() {}.type),

                    Gson().fromJson(
                            strOccupants,
                            object : TypeToken<List<Int>>() {}.type)
            )
        }
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

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.getFilter()?.filter(newText)
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

        adapter.setLoadingOffset(1)
        recyclerview.adapter = adapter
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
                viewModel.initFriendsSelected()
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
                    if (viewModel.itemsSelected?.size() > 0) {
                        var name = ""
                        if (viewModel.itemsSelected.size() == 1) {
                            // private chat
                            name = getString(R.string.display_name, viewModel.itemsSelected?.get(0)?.firstName, viewModel.itemsSelected?.get(0)?.lastName)
                        } else {
                            // group chat
                            name = "Group " + System.currentTimeMillis()
                        }
                        EventBus.getDefault().post(FriendsSelectEvent(Functions.sparrArrToArr(viewModel.itemsSelected), name, from))
                        activity.finish()
                    } else {
                        Functions.showToastShortMessage(activity, "Please select at least 1 friend")
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