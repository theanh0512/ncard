package com.user.ncard.ui.chats.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.*
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.barryzhang.temptyview.TViewUtil
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBDialogType
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChatListDialogsBinding
import com.user.ncard.ui.card.AddFriendAndNameCardActivity
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.utils.EndlessRecyclerAdapter
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.GroupDialogEvent
import com.user.ncard.ui.chats.GroupDialogUpdateEvent
import com.user.ncard.ui.chats.broadcast.BroadcastGroupListActivity
import com.user.ncard.ui.chats.detail.ChatActivity
import com.user.ncard.ui.chats.friends.FriendsListActivity
import com.user.ncard.ui.chats.friends.FriendsSelectEvent
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.ui.chats.utils.gcm.GcmConsts
import com.user.ncard.ui.group.GroupActivity
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.ChatDialog
import com.user.ncard.vo.Friend
import com.user.ncard.vo.QMMessageType
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_chat_list_dialogs.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ChatListFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, ChatDialogsAdapter.OnItemClickListener {

    val TAG = "ChatListFragment"

    companion object {
        fun newInstance(): ChatListFragment = ChatListFragment()
    }

    lateinit var viewModel: ChatListViewModel
    lateinit var fragmentBinding: FragmentChatListDialogsBinding
    lateinit var adapter: ChatDialogsAdapter

    @Inject
    lateinit var chatHelper: ChatHelper
    //lateinit var pushBroadcastReceiver: BroadcastReceiver

    override fun getLayout(): Int {
        return R.layout.fragment_chat_list_dialogs
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //initToolbar(getString(R.string.title_chat), false)
        toolBar = activity.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        if (supportActionBar != null) {
            (activity.findViewById<View>(R.id.text_view_action_bar) as TextView)?.setText(R.string.title_chat)
            (activity.findViewById<View>(R.id.tvMenuLeft) as TextView)?.setText(R.string.title_broadcast)
            (activity.findViewById<View>(R.id.tvMenuLeft) as TextView)?.setOnClickListener({
                startActivity(BroadcastGroupListActivity.getIntent(activity))
            })
        }
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChatListViewModel::class.java)
        fragmentBinding = FragmentChatListDialogsBinding.bind(rootView)
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

        //viewModel.initListener()
        //pushBroadcastReceiver = PushBroadcastReceiver()

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
            viewModel.start.value = true
        }

        EventBus.getDefault().register(this)
    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = ChatDialogsAdapter(activity, viewModel.getItems(), chatHelper, this)

        adapter.setLoadingOffset(1)
        adapter.setCallbacks(object : EndlessRecyclerAdapter.LoaderCallbacks {
            override fun canLoadNextItems(): Boolean {
                return viewModel.canLoadMore()
            }

            override fun loadNextItems() {
                viewModel.loadMore()
            }

        })
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
                if (it?.data != null && it?.data?.isNotEmpty()) {
                    notifyAdapterChange()
                    fragmentBinding.srl.isRefreshing = false
                }
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

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onFriendsSelectEvent(friendsSelectEvent: FriendsSelectEvent) {
        if (friendsSelectEvent.from != FriendsListActivity.CHAT_DIALOG_LIST) {
            return
        }
        // 2 cases: private, group
        if (friendsSelectEvent?.list != null) {
            // Private chat
            if (friendsSelectEvent?.list?.size == 1) {
                if (isPrivateDialogExisting(friendsSelectEvent?.list[0])) {
                    // Get available dialog
                    val chatDialog = getPrivateDialogWithUser(friendsSelectEvent?.list[0])
                    openChaDialogMessage(chatDialog)
                } else {
                    // Create private chat dialog
                    createNewChatDialog(friendsSelectEvent?.list, friendsSelectEvent?.name)
                }
            } else {
                // Create Group chat dialog
                showInputNameOfGroupChat(friendsSelectEvent?.list, friendsSelectEvent?.name)
            }
        }
    }

    fun showInputNameOfGroupChat(friends: List<Friend>, name: String?) {
        val alertDialogBuilder = AlertDialog.Builder(
                context)
        //Functions.hideSoftKeyboard(context as Activity?)
        val view = layoutInflater.inflate(R.layout.view_group_chat_name, null);
        val input = view?.findViewById<EditText>(R.id.edtGroupName);

        alertDialogBuilder.setTitle("Group Chat")
        alertDialogBuilder
                .setMessage("")
                .setCancelable(false)
                .setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->

                })
                .setNegativeButton(R.string.cancel, { dialog, id ->
                    dialog.dismiss()
                })

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setView(view)
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
            if (input?.text.toString().isNullOrBlank()) {
                Functions.showToastShortMessage(activity, "Please input group chat name")
            } else {
                createNewChatDialog(friends, input?.text.toString())
                alertDialog.dismiss()
            }
        })
    }


    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onChatDialogUpdateEvent(chatDialogUpdateEvent: ChatDialogUpdateEvent) {
        viewModel?.updateDialog(chatDialogUpdateEvent?.dialogId)
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onGroupDialogUpdateEvent(groupDialogUpdateEvent: GroupDialogUpdateEvent) {
        viewModel?.reLoadFromDb()
    }

    private fun isPrivateDialogExisting(friend: Friend): Boolean {
        friend?.let {
            if (getPrivateDialogWithUser(friend) != null) {
                return true
            }
        }
        return false
    }

    private fun getPrivateDialogWithUser(user: Friend): ChatDialog? {
        viewModel?.items?.value?.data?.let {
            for (chatDialog in viewModel?.items?.value?.data!!) {
                if (QBDialogType.PRIVATE.code == chatDialog?.type && chatDialog?.occupantIds?.contains(user.chatId)!!) {
                    return chatDialog
                }
            }
        }
        return null
    }

    private fun openChaDialogMessage(chatDialog: ChatDialog?) {
//        showProgressDialog()
//        chatHelper.getDialogById(chatDialog?.dialogId!!, object : QBEntityCallback<QBChatDialog> {
//            override fun onError(qbResponseException: QBResponseException?) {
//                showSnackbarMessage(qbResponseException?.message)
//            }
//
//            override fun onSuccess(qbChatDialog: QBChatDialog?, bundle: Bundle?) {
//                hideProgressDialog()
        startActivity(ChatActivity.getIntent(activity, chatDialog?.dialogId!!))
//            }
//        })
    }

    private fun createNewChatDialog(friends: List<Friend>, name: String?) {
        showProgressDialog()
        val occupantIds = friends.map { friend -> friend.chatId }
        chatHelper.createChatDialog(occupantIds as List<Int>, name, object : ChatHelper.CreateChatDialogListener {
            override fun onCreateSuccess(qbChatDialog: QBChatDialog) {
                // Insert to db and open new chat
                hideProgressDialog()
                viewModel.chatRepository?.insertDialogToDb(qbChatDialog)
                // Send system message about creating group dialog
                if (qbChatDialog.type == QBDialogType.GROUP) {
                    EventBus.getDefault().post(GroupDialogEvent(qbChatDialog, null, QMMessageType.createGroupDialog.type))
                }
                startActivity(ChatActivity.getIntent(activity, qbChatDialog.dialogId))
            }

            override fun onCreateError() {
                hideProgressDialog()
            }
        })
    }

    override fun onItemClick(chatDialog: ChatDialog, position: Int) {
        openChaDialogMessage(chatDialog)
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_add, menu)
        val menuItem: MenuItem? = menu?.findItem(R.id.menu_item_add)
        val actionView = menuItem?.actionView
        actionView?.setOnClickListener {
            startActivity(FriendsListActivity.getIntent(activity, FriendsListActivity.CHAT_DIALOG_LIST, null, null))
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
        /*R.id.menu_item_broadcast -> {
            consume {
                startActivity(BroadcastGroupListActivity.getIntent(activity))
            }
        }*/
            R.id.menu_item_add -> {
                consume {
                    startActivity(FriendsListActivity.getIntent(activity, FriendsListActivity.CHAT_DIALOG_LIST, null, null))
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onResume() {
        super.onResume()
        /*LocalBroadcastManager.getInstance(activity).registerReceiver(pushBroadcastReceiver,
                IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT))*/
    }

    override fun onPause() {
        super.onPause()
        /*LocalBroadcastManager.getInstance(activity).unregisterReceiver(pushBroadcastReceiver)*/
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        //viewModel.unregisterListener()
    }

    /*private inner class PushBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE)
            Log.v(TAG, "Received broadcast " + intent.action + " with data: " + message)
            *//*requestBuilder.setSkip(skipRecords = 0)
            loadDialogsFromQb(true, true)*//*
        }
    }*/

}