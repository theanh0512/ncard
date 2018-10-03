package com.user.ncard.ui.chats.groups

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.config.BoxingCropOption
import com.bilibili.boxing.model.config.MediaFilter
import com.bilibili.boxing.model.entity.impl.ImageMedia
import com.bilibili.boxing.utils.BoxingFileHelper
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.google.gson.Gson
import com.quickblox.chat.model.QBDialogType
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChatGroupDetailBinding
import com.user.ncard.ui.card.catalogue.post.CataloguePostFragment
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.Constants
import com.user.ncard.ui.catalogue.S3TransferUtil
import com.user.ncard.ui.catalogue.utils.CallbackAlertDialogListener
import com.user.ncard.ui.catalogue.utils.CompressionUtil
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.ui.chats.GroupDialogEvent
import com.user.ncard.ui.chats.detail.ChatActivity
import com.user.ncard.ui.chats.friends.FriendsListActivity
import com.user.ncard.ui.chats.friends.FriendsSelectEvent
import com.user.ncard.ui.chats.utils.ChatMessageActionHolder
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.SwipeToDeleteCallback
import com.user.ncard.vo.Friend
import com.user.ncard.vo.QMMessageType
import com.user.ncard.vo.Status
import kotlinx.android.synthetic.main.fragment_chat_group_detail.*
import me.shaohui.advancedluban.OnMultiCompressListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ChatGroupDetailFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {
    val TAG = "ChatGroupDetailFragment"

    companion object {
        const val TYPE_VIEW = 0
        const val TYPE_CREATE = 1
        const val TYPE_UPDATE = 2
        fun newInstance(): ChatGroupDetailFragment = ChatGroupDetailFragment()
    }

    lateinit var viewModel: ChatGroupDetailViewModel
    lateinit var fragmentBinding: FragmentChatGroupDetailBinding
    lateinit var adapter: ChatGroupMemberListAdapter

    @Inject
    lateinit var chatHelper: ChatHelper

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper
    @Inject lateinit var ffmpeg: FFmpeg
    lateinit var compressionUtil: CompressionUtil

    lateinit var s3TransferUtil: S3TransferUtil

    override fun getLayout(): Int {
        return R.layout.fragment_chat_group_detail
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_group_detail), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChatGroupDetailViewModel::class.java)
        fragmentBinding = FragmentChatGroupDetailBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        val dialogId = bundle.getString(ChatActivity.EXTRA_DIALOG_ID)
        viewModel.initData(dialogId)
        initObserDialog()
        viewModel.type = TYPE_UPDATE
        // viewModel.initData()
    }

    override fun init() {
        s3TransferUtil = S3TransferUtil(activity, Constants.FOLDER_CHAT)
        compressionUtil = CompressionUtil(context, ffmpeg)
        // Load data here
        if (viewModel.qbChatDialog != null && !viewModel.qbChatDialog?.dialogId.isBlank()) { // Wating for getting chat dialog
            initObser()
            initData()
            initAdapter()
        }
        iniSwipeRefreshLayout()

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
        }

        fragmentBinding.viewAddNewMember.setOnClickListener(View.OnClickListener {
            startActivity(FriendsListActivity.getIntent(activity,
                    if (viewModel.qbChatDialog.userId == chatHelper.getCurrentQBUser().id) FriendsListActivity.CHAT_GROUP_MANAGE_AS_ADMIN else FriendsListActivity.CHAT_GROUP_MANAGE_AS_MEMBER,
                    if (viewModel.getGroupOccupantIdsSelected() != null) Gson().toJson(viewModel.getGroupOccupantIdsSelected()) else null,
                    if (viewModel.qbChatDialog.occupants != null) Gson().toJson(viewModel.qbChatDialog.occupants) else null))
        })

        fragmentBinding.viewDeleteGroupName.setOnClickListener(View.OnClickListener {
            Functions.showAlertDialogYesNo(activity, "", "Do you want to leave this group?", object : CallbackAlertDialogListener {
                override fun onClickOk() {
                    viewModel.exit.value = true
                }

                override fun onClickCancel() {
                }
            })
        })

        fragmentBinding.imvGroup.setOnClickListener(View.OnClickListener {
            val cachePath = BoxingFileHelper.getCacheDir(activity)
            if (TextUtils.isEmpty(cachePath)) {
                Toast.makeText(activity, R.string.boxing_storage_deny, Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val destUri = Uri.Builder()
                    .scheme("file")
                    .appendPath(cachePath)
                    .appendPath(String.format(Locale.US, "%s.png", System.currentTimeMillis()))
                    .build()
            val singleImgConfig = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).withCropOption(BoxingCropOption(destUri).aspectRatio(1.0F, 1.0F)).needCamera(R.drawable.ic_boxing_camera).mediaFilter(MediaFilter(0, 0, 10))
            Boxing.of(singleImgConfig).withIntent(activity, BoxingActivity::class.java).start(activity, CataloguePostFragment.IMAGE_REQUEST_CODE)
        })

        EventBus.getDefault().register(this)
    }

    fun initValue() {
        fragmentBinding.edtGroupName.setText(viewModel?.qbChatDialog.name)
        fragmentBinding.edtGroupName.isEnabled = (viewModel?.qbChatDialog?.userId == chatHelper.getCurrentQBUser().id)
        fragmentBinding.imvGroup.isEnabled = (viewModel?.qbChatDialog?.userId == chatHelper.getCurrentQBUser().id)
        GlideHelper.displaySquareAvatar(fragmentBinding.imvGroup, viewModel?.qbChatDialog?.photo);
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
        adapter = ChatGroupMemberListAdapter(activity, viewModel.getItems(), viewModel.allFriendsInLocalDb, chatHelper.getCurrentQBUser()?.id, viewModel.qbChatDialog.userId)

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
        if (viewModel.qbChatDialog.userId == chatHelper.getCurrentQBUser().id) {
            // this is admin
            swipeHandler.disableAt = 0
        } else {
            swipeHandler.disableAt = -2
        }
    }

    fun iniSwipeRefreshLayout() {
        fragmentBinding.srl.setColorSchemeResources(R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue,
                R.color.colorDarkBlue)
        fragmentBinding.srl.isEnabled = false
        fragmentBinding.srl.setOnRefreshListener(this)
    }

    fun initObserDialog() {
        viewModel.chatDialog.observe(this, android.arch.lifecycle.Observer {
            if (it != null) {
                //Functions.showToastShortMessage(activity, "Loading dialog finish")
                if (viewModel.qbChatDialog == null || viewModel.qbChatDialog?.dialogId.isBlank()) {
                    // init QbChatDialog
                    viewModel.initQbChatDialog()
                    viewModel.getUserNotFriend()
                    initObser()
                    initData()
                    initAdapter()
                } else {
                    // Update QbChatDialog
                    viewModel.initQbChatDialog()
                    // Load users again
                    viewModel.getUserNotFriend()
                }
            } else {
                // Kicked out of dialog/User leaves group
                activity?.finish()
            }
        })
    }

    fun initObser() {
        viewModel.userNotFriends.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                //showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                //hideProgressDialog()
                viewModel.processUserNotFriends()
                viewModel.prepareListFriends()
                loadingFinish()
                notifyAdapterChange()
            } else if (it?.status == Status.ERROR) {
                viewModel.prepareListFriends()
                showSnackbarMessage(it?.message)
                loadingFinish()
                notifyAdapterChange()

            }
        })

        // Listen exit  group
        viewModel.exitItem.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                Toast.makeText(activity, "Exit group successfully", Toast.LENGTH_LONG).show()
                hideProgressDialog()
                // Send system message about updating group dialog
                if (it?.data?.type == QBDialogType.GROUP) {
                    EventBus.getDefault().post(GroupDialogEvent(it?.data!!, arrayListOf(Functions.getMyChatId(sharedPreferenceHelper)), QMMessageType.updateGroupDialog.type))
                }
                activity.finish()
            } else if (it?.status == Status.ERROR) {
                Toast.makeText(activity, it?.message, Toast.LENGTH_LONG).show()
                showSnackbarMessage(it?.message)
            } else {
                loadingFinish()
            }
        })
        // Listen Update group
        viewModel.updateItem.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                Toast.makeText(activity, "Update group successfully", Toast.LENGTH_LONG).show()
                hideProgressDialog()
                // Send system message about updating group dialog
                if (it?.data?.type == QBDialogType.GROUP) {
                    // Find occupantIds Removed
                    var occupantIdsRemoved: List<Int>? = ArrayList<Int>()
                    if (it?.data?.occupants.size > 0) {
                        occupantIdsRemoved = viewModel.qbChatDialog.occupants?.filter { id -> !it?.data?.occupants.contains(id) }
                    }
                    EventBus.getDefault().post(GroupDialogEvent(it?.data, occupantIdsRemoved, QMMessageType.updateGroupDialog.type))
                }
                viewModel.qbChatDialog = it?.data!!
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
        adapter.updateItems(viewModel.getItems(), viewModel.allFriendsInLocalDb)
        adapter.notifyDataSetChanged()
        /*TViewUtil.EmptyViewBuilder.getInstance(activity)
                .bindView(recyclerview)*/
        initData()
        initValue()
        activity.invalidateOptionsMenu()
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onFriendsSelectEvent(friendsSelectEvent: FriendsSelectEvent) {
        if (friendsSelectEvent.from != FriendsListActivity.CHAT_GROUP_MANAGE_AS_MEMBER || friendsSelectEvent.from != FriendsListActivity.CHAT_GROUP_MANAGE_AS_ADMIN) {
            return
        }
        if (friendsSelectEvent?.list != null && friendsSelectEvent?.list.isNotEmpty()) {
            //val you = viewModel.listFriends.get(0) // you current info in group
            //viewModel.listFriends = ArrayList<Friend>(friendsSelectEvent?.list)
            // Find friends removed
            var friendsRemoved: MutableList<Friend> = ArrayList<Friend>()
            viewModel.listFriends?.forEach {
                // not check current user and user not friend
                if (it.chatId != chatHelper.getCurrentQBUser().id && !findFriendInFriedsList(it.id, viewModel.listUserNotFriends)) {
                    if (!findFriendInFriedsList(it.id, friendsSelectEvent?.list)) {
                        friendsRemoved.add(it)
                    }
                }
            }
            // Find friends added
            var friendsAdded: MutableList<Friend> = ArrayList<Friend>()
            friendsSelectEvent?.list?.forEach {
                if (!findFriendInFriedsList(it.id, viewModel.listFriends)) {
                    friendsAdded.add(it)
                }
            }
            // Process friends removed
            viewModel.listFriends?.removeAll(friendsRemoved)
            // Process friends added
            viewModel.listFriends.addAll(friendsAdded)

            notifyAdapterChange()
        }
    }

    fun findFriendInFriedsList(id: Int, friendsList: List<Friend>?): Boolean {
        friendsList?.forEach {
            if (it.id == id) {
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CataloguePostFragment.IMAGE_REQUEST_CODE) {
                val medias = Boxing.getResult(data)
                val baseMedia = medias?.get(0)
                (baseMedia != null && baseMedia is ImageMedia).let {
                    (baseMedia as ImageMedia).removeExif()
                    // No need to compress and upload to server now
                    mediaUpload = baseMedia?.path
                    // Just update the UI locally
                    BoxingMediaLoader.getInstance().displayThumbnail(fragmentBinding.imvGroup, mediaUpload!!, 150, 150)
                }
            }
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
                    if (fragmentBinding.edtGroupName.text.toString()?.isNotEmpty() && viewModel.getItems()?.isNotEmpty()) {
                        // check the differences
                        var hasChanged = false
                        // 2 cases here: 1: change name and occupant id -> update dialog right now, 2: change photo of group -> upload first, then update dialog
                        if (mediaUpload == null) {   // No change group photo
                            if (fragmentBinding.edtGroupName.text.toString() != viewModel.qbChatDialog?.name) {
                                hasChanged = true
                            }
                            if (!hasChanged) {
                                val newOccupantIds = viewModel.listFriends.map { it.chatId }
                                if (newOccupantIds.containsAll(viewModel.qbChatDialog.occupants) && viewModel.qbChatDialog.occupants.containsAll(newOccupantIds)) {
                                    //
                                } else {
                                    hasChanged = true
                                }
                            }
                            if (hasChanged) {
                                viewModel.updateGroupDialog(fragmentBinding.edtGroupName.text.toString(), null)
                            } else {
                                Functions.showLogMessage(TAG, "no changes")
                            }
                        } else {
                            // Has change in group photo, compress and upload photo
                            compressMedia()
                        }
                    } else {
                        Functions.showToastShortMessage(activity, "Please add group name and select friends")
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

    /* Medias Upload */
    var mediaUpload: String? = null
    var typeUpload = -1
    fun compressMedia() {
        if (mediaUpload == null) {
            // Do nothing
        } else {
            if (Functions.isImageFile(mediaUpload!!)) {
                showProgressDialog()
                typeUpload = S3TransferUtil.TYPE_IMAGES
                compressionUtil.compressImage(arrayListOf(mediaUpload) as List<String>, object : OnMultiCompressListener {
                    override fun onSuccess(fileList: MutableList<File>?) {
                        uploadMedia(fileList?.map { it.absolutePath }!!, S3TransferUtil.TYPE_IMAGES)
                    }

                    override fun onError(e: Throwable?) {
                        showSnackbarMessage(e?.message)
                    }

                    override fun onStart() {
                    }

                })
            }
        }
    }

    fun uploadMedia(lsPath: List<String>, type: Int) {
        //initViewUpload()
        s3TransferUtil.beginUploads(
                lsPath, type, object : S3TransferUtil.TrackingUpload {

            override fun onProgressChanged(file: File?, id: Int, bytesCurrent: Long, bytesTotal: Long) {
            }

            override fun onFinishAll(fileNames: MutableList<String>?, type: Int) {
                // Update dialog
                if (type == S3TransferUtil.TYPE_IMAGES) {
                    viewModel.updateGroupDialog(fragmentBinding.edtGroupName.text.toString(), fileNames?.get(0))
                }
                // Reset media
                mediaUpload = null
                hideProgressDialog()
            }

            override fun onFinishAt(index: Int) {
            }

        })
    }
    /* Medias Upload */

}