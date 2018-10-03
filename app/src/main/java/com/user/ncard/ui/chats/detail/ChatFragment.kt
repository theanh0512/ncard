package com.user.ncard.ui.chats.detail

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.alexvasilkov.gestures.animation.ViewPositionAnimator
import com.alexvasilkov.gestures.commons.DepthPageTransformer
import com.alexvasilkov.gestures.transition.GestureTransitions
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator
import com.alexvasilkov.gestures.transition.tracker.FromTracker
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker
import com.barryzhang.temptyview.TViewUtil
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.BoxingMediaLoader
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.config.MediaFilter
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.gson.Gson
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChatBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.catalogue.post.CataloguePostFragment
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.Constants
import com.user.ncard.ui.catalogue.S3TransferUtil
import com.user.ncard.ui.catalogue.main.Image
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.ui.catalogue.mediaviewer.FullScreenVideoPlayerActivity
import com.user.ncard.ui.catalogue.mediaviewer.ImagesPagerAdapter
import com.user.ncard.ui.catalogue.utils.CallbackAlertDialogListener
import com.user.ncard.ui.catalogue.utils.CompressionUtil
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.SendGiftEvent
import com.user.ncard.ui.chats.dialogs.ChatDialogUpdateEvent
import com.user.ncard.ui.chats.forward.ForwardListActivity
import com.user.ncard.ui.chats.forward.ForwardSelectEvent
import com.user.ncard.ui.chats.friends.FriendsListActivity
import com.user.ncard.ui.chats.groups.ChatGroupDetailActivity
import com.user.ncard.ui.chats.shipping.ShippingAddressActivity
import com.user.ncard.ui.chats.shipping.ShippingEvent
import com.user.ncard.ui.chats.utils.ChatMessageActionHolder
import com.user.ncard.ui.chats.utils.audiorecord.AudioRecorder
import com.user.ncard.ui.chats.utils.audiorecord.MediaRecorderException
import com.user.ncard.ui.chats.utils.audiorecord.QBMediaRecordListener
import com.user.ncard.ui.chats.views.*
import com.user.ncard.ui.me.ewallet.roundTo2DecimalPlaces
import com.user.ncard.ui.me.gift.MyGiftActivity
import com.user.ncard.ui.me.gift.MyGiftFragment
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.*
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.layout_images_pager.*
import kotlinx.android.synthetic.main.view_chat_media_upload.*
import kotlinx.android.synthetic.main.view_dialog_accept_gift.*
import me.piruin.quickaction.ActionItem
import me.piruin.quickaction.QuickAction
import me.shaohui.advancedluban.OnMultiCompressListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.XMPPConnection
import java.io.File
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by dangui on 13/11/17.
 */
class ChatFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, Injectable, ViewPositionAnimator.PositionUpdateListener, MessageListAdapter.MessageClickCallback {

    private val TAG = "ChatFragment"
    val PLACE_PICKER_REQUEST = 1

    companion object {
        fun newInstance(): ChatFragment = ChatFragment()
    }

    lateinit var viewModel: ChatViewModel
    lateinit var fragmentBinding: FragmentChatBinding
    lateinit var adapter: MessageListAdapter

    @Inject
    lateinit var chatHelper: ChatHelper
    @Inject lateinit var ffmpeg: FFmpeg
    lateinit var compressionUtil: CompressionUtil

    lateinit var s3TransferUtil: S3TransferUtil

    override fun getLayout(): Int {
        return R.layout.fragment_chat
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // initToolbar(getString(R.string.title_chat_detail), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChatViewModel::class.java)
        fragmentBinding = FragmentChatBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        val dialogId = bundle.getString(ChatActivity.EXTRA_DIALOG_ID)
        viewModel.initData(dialogId)
        initObserDialog()
    }

    override fun init() {
        s3TransferUtil = S3TransferUtil(activity, Constants.FOLDER_CHAT)
        compressionUtil = CompressionUtil(context, ffmpeg)
        // Load data here
        if (viewModel.qbChatDialog != null && !viewModel.qbChatDialog?.dialogId.isBlank()) { // Wating for getting chat dialog
            initObser()
            initAdapter()
            setupPrivateChat()
            // Init toolbar again
            iniToolbar()
            initQuickAction()
        }
        iniSwipeRefreshLayout()
        initImagesPager()
        initAnimator()
        setCopyPasteEdtChat()
        initAudioRecorder()


        /*val loader = BoxingFrescoLoader(activity)
        BoxingMediaLoader.getInstance().init(loader)*/

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
            viewModel.start.value = true
        }

        fragmentBinding.buttonChatSend.setOnClickListener {
            val textToSend = fragmentBinding.editChatMessage.text.toString()
            if (textToSend.isNotBlank()) {
                sendMessage(viewModel.qbChatDialog, textToSend, ChatMessageContentType.TEXT.type, null, null, null, null, null)
            }
            fragmentBinding.editChatMessage.setText("")
        }

        fragmentBinding.buttonAudio.setOnClickListener({
            startRecord()
        })
        fragmentBinding.buttonMenuAdd.setOnClickListener({
            quickActionChat.show(fragmentBinding.buttonMenuAdd)
        })

        EventBus.getDefault().register(this)
    }

    /* Send message */
    fun sendMessage(qbChatDialog: QBChatDialog, text: String?, type: String, chat_file: String?, chat_location: String?, credit_transaction: String?, gift: String?, system_info: String?) {
        viewModel.sendChatMessage(qbChatDialog, text, type, chat_file, chat_location, credit_transaction, gift, system_info)

        viewModel.chatMessage.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                //showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                //hideProgressDialog()
            } else if (it?.status == Status.ERROR) {
                showSnackbarMessage(it?.message)
            }
        })
    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = MessageListAdapter(activity, viewModel.getItems(),
                viewModel.qbChatDialog, chatHelper, this, viewModel.sharedPreferenceHelper)

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

    fun iniToolbar() {
        // Init toolbar again
        if (viewModel?.qbChatDialog != null) {
            initToolbar(viewModel?.qbChatDialog?.name, true)
        }
    }

    fun initObserDialog() {
        viewModel.chatDialog.observe(this, android.arch.lifecycle.Observer {
            if (it != null) {
                //Functions.showToastShortMessage(activity, "Loading dialog finish")
                if (viewModel.qbChatDialog == null || viewModel.qbChatDialog?.dialogId.isBlank()) {
                    // init QbChatDialog
                    viewModel.initQbChatDialog()
                    setupPrivateChat()
                    initObser()
                    initAdapter()
                    iniToolbar()
                    initQuickAction()
                    if (viewModel.qbChatDialog.type.code == ChatDialogType.GROUP.type) {
                        viewModel.joinGroupChat(object : QBEntityCallback<Void> {
                            override fun onSuccess(result: Void?, b: Bundle?) {
                                Functions.showLogMessage(TAG, "Joined group chat")
                            }

                            override fun onError(error: QBResponseException?) {
                                showSnackbarMessage(error?.message)
                            }
                        })
                    }
                } else {
                    // Update QbChatDialog
                    viewModel.updateQbChatDialog()
                    iniToolbar()
                }
            } else {
                // Kicked out of dialog/User leaves group
                activity?.finish()
            }
        })
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

        viewModel.forwardException.observe(this, Observer {
            if (it != null) {
                Functions.showSnackbarLongMessage(view, it.message)
            }
        })

        viewModel.forwardSuccess.observe(this, Observer {
            if (it != null) {
                Functions.showLogMessage("Trong", "Forward success with " + it?.id)
                Functions.showToastShortMessage(activity, "Message Forwarded")
            }
        })

        viewModel.walletInfo.observe(this, Observer {
            if (it != null) {
                Functions.showLogMessage(TAG, it?.toString())
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
        val isAdded = adapter.itemCount < viewModel.getItems().size
        adapter.updateItems(viewModel.getItems())
        adapter.notifyDataSetChanged()
        if (isAdded) scrollToBottom()
        TViewUtil.EmptyViewBuilder.getInstance(activity)
                .bindView(recyclerview)
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    fun setupPrivateChat() {
        viewModel.setupActivePrivateChatDialog(object : QBChatDialogMessageListener {
            override fun processMessage(dialogId: String?, qbChatMessage: QBChatMessage?, senderId: Int?) {
                Utils.Log(TAG, "<- receive Message - dialogId:$dialogId qbChatMessage:$qbChatMessage")
                viewModel.processReceivedMessage(viewModel.qbChatDialog, qbChatMessage)
            }

            override fun processError(dialogId: String?, exception: QBChatException?, qbChatMessage: QBChatMessage?, senderId: Int?) {
                var message = exception?.localizedMessage
                if (message.isNullOrBlank() && viewModel.qbChatDialog.type == QBDialogType.PRIVATE) {
                    // This is blocked user
                    message = "Cannot send message to this user"
                    // Remove message stored to db
                    viewModel.processRemovedMessage(viewModel.qbChatDialog, qbChatMessage)
                }
                Functions.showSnackbarShortMessage(view, message)
                Utils.Log(TAG, "<- receive Message - dialogId:$dialogId qBChatException:${message} qbChatMessage:$qbChatMessage")
            }
        })
        initConnectionListener()
    }

    fun initConnectionListener() {
        viewModel.chatConnectionListener = object : ConnectionListener {
            override fun connected(p0: XMPPConnection?) {
                Log.e(TAG, "connected()")
            }

            override fun connectionClosed() {
                Log.e(TAG, "connectionClosed()")
            }

            override fun connectionClosedOnError(e: Exception?) {
                Log.e(TAG, "connectionClosedOnError(): " + e?.getLocalizedMessage())
            }

            override fun reconnectionSuccessful() {
            }

            override fun authenticated(p0: XMPPConnection?, p1: Boolean) {
                Log.e(TAG, "authenticated()")
            }

            override fun reconnectionFailed(error: Exception?) {
                Log.e(TAG, "reconnectionFailed(): " + error?.getLocalizedMessage())
            }

            override fun reconnectingIn(seconds: Int) {
                Log.e(TAG, "reconnectingIn(): " + seconds)
            }

        }
    }

    private fun scrollToBottom() {
        if (adapter.items.size > 1) {
            recyclerview.scrollToPosition(adapter.items.size - 1)
        }
    }

    override fun onDestroy() {
        QBRestChatService.markMessagesAsRead(viewModel.qbChatDialog.dialogId, null).performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                EventBus.getDefault().post(ChatDialogUpdateEvent(viewModel.qbChatDialog.dialogId))
            }

            override fun onError(e: QBResponseException?) {}
        })
        releaseChat()
        /*if (viewModel.qbChatDialog.type == QBDialogType.PRIVATE) {
            var opponent: Int? = null
            viewModel.qbChatDialog.occupants.forEach {
                if (it != Functions.getMyChatId(viewModel.sharedPreferenceHelper)) {
                    opponent = it
                    return@forEach
                }
            }
            if (opponent != null) {
                EventBus.getDefault().post(PrivacyListEvent(viewModel.qbChatDialog, opponent.toString(), false))
            }
        }*/
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun releaseChat() {
        viewModel.removeActivePrivateChatListener(viewModel.qbChatDialog)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                val place: Place = PlacePicker.getPlace(activity, data)
                //Functions.showToastShortMessage(activity, place.getName().toString() + " | " + place.getAddress())
                val chatLocation = ChatMessage.ChatLocation(place?.latLng?.latitude!!, place?.latLng?.longitude!!, place.name.toString(), place.address.toString())
                sendMessage(viewModel.qbChatDialog, null, ChatMessageContentType.LOCATION.type, null, Gson().toJson(chatLocation), null, null, null)
            } else if (requestCode == CataloguePostFragment.IMAGE_REQUEST_CODE || requestCode == CataloguePostFragment.VIDEO_REQUEST_CODE) {
                val medias = Boxing.getResult(data)
                (medias != null && medias.isNotEmpty()).let {
                    mediaUpload = medias?.get(0)?.path
                    compressMedia()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatHelper.getCurrentChatService().addConnectionListener(viewModel.chatConnectionListener)
    }

    override fun onPause() {
        super.onPause()
        chatHelper.getCurrentChatService().removeConnectionListener(viewModel.chatConnectionListener)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val itemDoSth = menu?.findItem(R.id.menu_item_do)
        itemDoSth?.setTitle("")
        itemDoSth?.setIcon(activity.getDrawable(R.drawable.ic_group_chat_detail))
        if (viewModel?.qbChatDialog?.type.code == ChatDialogType.GROUP.type) {
            itemDoSth?.isVisible = true
        } else {
            itemDoSth?.isVisible = false
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
                    startActivity(ChatGroupDetailActivity.getIntent(activity, viewModel.qbChatDialog.dialogId))
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onLongClick(message: ChatMessage, position: Int) {
        // Update quick action
        val copyItem = ActionItem(ID_COPY, "Copy")
        val forwardItem = ActionItem(ID_FORWARD, "Forward")
        if (message.customParam.chat_content_type == ChatMessageContentType.TEXT.type
                || message.customParam.chat_content_type == ChatMessageContentType.IMAGE.type) {

            if (quickActionMessage.getActionItemById(ID_COPY) == null) {
                quickActionMessage.addActionItem(ID_COPY, copyItem)
            }
        } else if (message.customParam.chat_content_type == ChatMessageContentType.VIDEO.type
                || message.customParam.chat_content_type == ChatMessageContentType.AUDIO.type
                || message.customParam.chat_content_type == ChatMessageContentType.LOCATION.type) {
            if (quickActionMessage.getActionItemById(ID_COPY) != null) {
                quickActionMessage.remove(ID_COPY)
            }
            if (quickActionMessage.getActionItemById(ID_FORWARD) == null) {
                quickActionMessage.addActionItem(ID_FORWARD, forwardItem)
            }
        } else if (message.customParam.chat_content_type == ChatMessageContentType.CREDIT.type
                || message.customParam.chat_content_type == ChatMessageContentType.GIFT.type) {
            // TODO: ActionItem has error when remove id, error at remove item at position 0 (when remove seperator) => removeAt(-1)
            //
        }
        // show quick action
        val holder = recyclerview.findViewHolderForLayoutPosition(position)
        if (holder != null) {
            val bubbleChatSelected = adapter.getBubleChatLongClick(holder)
            bubbleChatSelected?.let {
                if (message.customParam.chat_content_type == ChatMessageContentType.CREDIT.type
                        || message.customParam.chat_content_type == ChatMessageContentType.GIFT.type) {
                    quickActionDelete.show(bubbleChatSelected)
                } else {
                    quickActionMessage.show(bubbleChatSelected)
                }
            }
        }
        // Hold chat message
        ChatMessageActionHolder.getInstance().chatMessage = message
    }

    override fun onItemClick(item: ChatMessage, position: Int) {
        Functions.showToastShortMessage(context, "onItemClick")
    }

    override fun onImageClick(item: ChatMessage, position: Int) {
        itemPosition = position
        val medias: List<Media> = arrayListOf(Image(item?.customParam?.chat_file?.remoteUrl!!))
        imagesPagerAdapter.setImages(medias)
        imagesPagerAdapter.setActivated(true)
        animator.enter(imagePosition, true)
    }

    override fun onVideoClick(item: ChatMessage, position: Int) {
        activity.startActivity(FullScreenVideoPlayerActivity.getIntent(activity, item?.customParam?.chat_file?.remoteUrl))
    }

    override fun onLocationClick(item: ChatMessage, position: Int) {
        Functions.openMap(activity, item?.customParam?.chat_location?.lat.toString(), item?.customParam?.chat_location?.lng.toString(),
                item?.customParam?.chat_location?.address)
    }

    var dilgAcceptTransferCredit: DialogAcceptTransferCredit? = null
    var dilgInfoTransferCredit: DialogInfoTransferCredit? = null

    fun getDialogAcceptTransferCredit(callback: DialogAcceptTransferCredit.IDialogAcceptTransferCredit): DialogAcceptTransferCredit? {
        if (dilgAcceptTransferCredit == null) {
            dilgAcceptTransferCredit = DialogAcceptTransferCredit(activity, callback)
        }
        return dilgAcceptTransferCredit
    }

    fun getDialogInfoTransferCredit(): DialogInfoTransferCredit? {
        if (dilgInfoTransferCredit == null) {
            dilgInfoTransferCredit = DialogInfoTransferCredit(activity, null)
        }
        return dilgInfoTransferCredit
    }

    override fun onCreditClick(message: ChatMessage, position: Int) {
        viewModel.getCreditTransactionDetail(message, message?.customParam?.credit_transaction?.id!!)

        viewModel.transferCreditResponse.observe(this, Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                if (it?.data?.status != message.customParam?.credit_transaction?.status) {
                    message.customParam?.credit_transaction = it.data
                }
                showCreditDialog(message, message.customParam?.credit_transaction!!, position)
                hideProgressDialog()
            } else if (it?.status == Status.ERROR) {
                hideProgressDialog()
                showSnackbarMessage(it?.message)
            }
        })
    }

    fun showCreditDialog(message: ChatMessage, credit_transaction: TransferCreditResponse, position: Int) {
        val money = getString(R.string.display_balance, credit_transaction?.amount?.amount?.toDouble()?.roundTo2DecimalPlaces().toString())
        if (viewModel.sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID) == credit_transaction?.sender?.id) {
            // Sender
            if (credit_transaction?.status == EWalletTransactionStatusType.ONHOLD.status) {
                getDialogInfoTransferCredit()?.start(money, credit_transaction.status, getString(R.string.credit_des_sender_on_hold))
            } else if (credit_transaction?.status == EWalletTransactionStatusType.REFUNDED.status) {
                getDialogInfoTransferCredit()?.start(money, credit_transaction.status, getString(R.string.credit_des_refund))
            } else if (credit_transaction?.status == EWalletTransactionStatusType.COMPLETED.status) {
                getDialogInfoTransferCredit()?.start(money, credit_transaction.status, getString(R.string.credit_des_transfer_accept))
            } else if (credit_transaction?.status == EWalletTransactionStatusType.FAILED.status) {

            }
        } else {
            // Receiver
            if (credit_transaction?.status == EWalletTransactionStatusType.ONHOLD.status) {
                getDialogAcceptTransferCredit(object : DialogAcceptTransferCredit.IDialogAcceptTransferCredit {
                    override fun onClickBtnSave(message: ChatMessage?, credit_transaction: TransferCreditResponse?) {
                        // Accept
                        updateCreditTransactionDetail(message!!, credit_transaction!!, position, EWalletTransactionAction.ACCEPT.action)
                    }

                    override fun onClickBtnCancel(message: ChatMessage?, credit_transaction: TransferCreditResponse?) {
                        // Reject
                        updateCreditTransactionDetail(message!!, credit_transaction!!, position, EWalletTransactionAction.REJECT.action)
                    }
                })?.start(message, credit_transaction, money, credit_transaction.status, getString(R.string.warn_refund))
            } else if (credit_transaction?.status == EWalletTransactionStatusType.REFUNDED.status) {
                getDialogInfoTransferCredit()?.start(money, credit_transaction.status, getString(R.string.credit_des_refund))
            } else if (credit_transaction?.status == EWalletTransactionStatusType.COMPLETED.status) {
                getDialogInfoTransferCredit()?.start(money, credit_transaction.status, getString(R.string.credit_des_transfer_accept))
            } else if (credit_transaction?.status == EWalletTransactionStatusType.FAILED.status) {

            }
        }
    }

    fun updateCreditTransactionDetail(message: ChatMessage, credit_transaction: TransferCreditResponse, position: Int, action: String) {
        viewModel.updateCreditTransactionDetail(message, message?.customParam?.credit_transaction?.id!!, RequestUpdateCreditTransaction(action))

        viewModel.transferCreditResponse.observe(this, Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                if (it?.data?.status != message.customParam?.credit_transaction?.status) {
                    message.customParam?.credit_transaction = it.data
                }
                val money = getString(R.string.display_balance, credit_transaction?.amount?.amount?.toDouble()?.roundTo2DecimalPlaces().toString())
                if (it?.data?.status == EWalletTransactionStatusType.COMPLETED.status) {
                    getDialogInfoTransferCredit()?.start(money, it?.data?.status, getString(R.string.credit_accept_success))
                } else {
                    getDialogInfoTransferCredit()?.start(money, it?.data?.status, getString(R.string.credit_reject_success))
                }
                // showCreditDialog(message, message.customParam?.credit_transaction!!, position)
                hideProgressDialog()
                // Send message update to chatting user
                val data = Gson().toJson(it?.data)
                val systemInfo = ChatMessage.ChatSystemInfoMessage(com.user.ncard.util.Constants.CATEGORY_WALLET, "transaction_update", data)
                if (systemInfo != null) {
                    sendMessage(viewModel.qbChatDialog, null, ChatMessageContentType.UXCSYSTEM.type, null, null, null, null, Gson().toJson(systemInfo))
                }
            } else if (it?.status == Status.ERROR) {
                hideProgressDialog()
                showSnackbarMessage(it?.message)
            }
        })
    }

    override fun onGiftClick(message: ChatMessage, position: Int) {
        viewModel.getGiftResponseDetail(message, message?.customParam?.gift?.id!!)

        viewModel.sendGiftResponse.observe(this, Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                if (it?.data?.status != message.customParam?.gift?.status) {
                    message.customParam?.gift = it.data
                }
                showGiftDialog(message, message.customParam?.gift!!)
                hideProgressDialog()
            } else if (it?.status == Status.ERROR) {
                hideProgressDialog()
                showSnackbarMessage(it?.message)
            }
        })
    }

    var dialogAcceptGift: DialogAcceptGift? = null
    var eCommerceGiftActionStatus: ECommerceGiftActionStatus = ECommerceGiftActionStatus.ACCEPT // For showing message after updating gift

    fun getDialogAcceptGift(callback: DialogAcceptGift.IDialogAcceptGift?): DialogAcceptGift? {
        if (dialogAcceptGift == null) {
            dialogAcceptGift = DialogAcceptGift(activity, callback)
        }
        return dialogAcceptGift
    }

    val giftCallback = object : DialogAcceptGift.IDialogAcceptGift {
        override fun onClickBtnAccept(chatMessage: ChatMessage?) {
            startActivity(ShippingAddressActivity.getIntent(activity, chatMessage?.messageId!!))
        }

        override fun onClickBtnCashout(chatMessage: ChatMessage?) {
            Functions.showAlertDialogYesNo(activity, "", getString(R.string.gift_cashout_confirm, 5.0), object : CallbackAlertDialogListener {
                override fun onClickOk() {
                    eCommerceGiftActionStatus = ECommerceGiftActionStatus.CASHOUT
                    updateGiftDetail(chatMessage!!, RequestUpdateGift(ECommerceGiftAction.CASHOUT.action, null, chatMessage.customParam?.gift?.message!!, null))
                }

                override fun onClickCancel() {
                }
            })
        }

        override fun onClickBtnSave(chatMessage: ChatMessage?) {
            eCommerceGiftActionStatus = ECommerceGiftActionStatus.SAVE
            updateGiftDetail(chatMessage!!, RequestUpdateGift(ECommerceGiftAction.ACCEPT.action, null, chatMessage.customParam?.gift?.message!!, null))
        }
    }

    fun showGiftDialog(message: ChatMessage, gift: SendGiftResponse) {
        if (viewModel.sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID) == gift?.sender?.id) {
            // Sender
            getDialogAcceptGift(giftCallback)?.start(message, gift, true)
        } else {
            // Receiver
            getDialogAcceptGift(giftCallback)?.start(message, gift, false)
        }
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onShippingEvent(shippingEvent: ShippingEvent) {
        if (shippingEvent != null) {
            val sendGiftResponse = shippingEvent?.chatMessage?.customParam?.gift
            eCommerceGiftActionStatus = ECommerceGiftActionStatus.ACCEPT
            val requestUpdate = RequestUpdateGift(ECommerceGiftAction.ACCEPT.action, null, sendGiftResponse?.message!!, shippingEvent.shipping)
            updateGiftDetail(shippingEvent.chatMessage, requestUpdate)
        }
    }

    fun updateGiftDetail(message: ChatMessage, request: RequestUpdateGift) {
        viewModel.updateGiftResponseDetail(message, message?.customParam?.gift?.id!!, request)

        viewModel.sendGiftResponse.observe(this, Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                if (it?.data?.status != message.customParam?.gift?.status) {
                    message.customParam?.gift = it.data
                }
                if (eCommerceGiftActionStatus == ECommerceGiftActionStatus.ACCEPT) {
                    //getDialogAcceptGift(giftCallback)?.start(context.getString(R.string.gift_status_accept_with_shipping), message.customParam?.gift!!)
                    getDialogInfoTransferCredit()?.startStatus(getString(R.string.gift_status_accept_with_shipping))
                } else {
                    var message = ""
                    if (eCommerceGiftActionStatus == ECommerceGiftActionStatus.CASHOUT) {
                        message = context.getString(R.string.gift_status_cashout)
                    } else if (eCommerceGiftActionStatus == ECommerceGiftActionStatus.SAVE) {
                        message = context.getString(R.string.gift_status_save)
                    }
                    Functions.showToastShortMessage(activity, message)
                }
                hideProgressDialog()
                // Send message update to chatting user
                val data = Gson().toJson(it?.data)
                val systemInfo = ChatMessage.ChatSystemInfoMessage(com.user.ncard.util.Constants.CATEGORY_ECOMMERCE, "update_gift", data)
                if (systemInfo != null) {
                    sendMessage(viewModel.qbChatDialog, null, ChatMessageContentType.UXCSYSTEM.type, null, null, null, null, Gson().toJson(systemInfo))
                }
            } else if (it?.status == Status.ERROR) {
                hideProgressDialog()
                showSnackbarMessage(it?.message)
            }
        })
    }

    override fun onAudioClick(item: ChatMessage, position: Int) {
    }
    // reject:  Money was refunded = > Money refunded
    // accept: Money was saved to your wallet => Transfer accepted


    /* Medias chat Upload */
    var mediaUpload: String? = null
    var typeUpload = -1
    fun compressMedia() {
        if (mediaUpload == null) {
        } else {
            if (Functions.isImageFile(mediaUpload!!)) {
                typeUpload = S3TransferUtil.TYPE_IMAGES
                compressionUtil.compressImage(arrayListOf(mediaUpload) as List<String>, object : OnMultiCompressListener {
                    override fun onSuccess(fileList: MutableList<File>?) {
                        uploadMedia(fileList?.map { it.absolutePath }!!, S3TransferUtil.TYPE_IMAGES)
                    }

                    override fun onError(e: Throwable?) {
                    }

                    override fun onStart() {
                    }

                })
            } else if (Functions.isVideoFile(mediaUpload!!)) {
                typeUpload = S3TransferUtil.TYPE_VIDEO
                compressionUtil.setupVideoCompress()
                compressionUtil.extractImageFromVideo(mediaUpload!!, object : FFmpegExecuteResponseHandler {
                    override fun onFailure(message: String?) {
                    }

                    override fun onProgress(message: String?) {
                    }

                    override fun onStart() {
                    }

                    override fun onSuccess(message: String?) {
                        compressionUtil.compressVideo(mediaUpload!!, object : FFmpegExecuteResponseHandler {
                            override fun onFinish() {
                            }

                            override fun onSuccess(message: String?) {
                                uploadMedia(arrayListOf(compressionUtil.videoOutputPath, compressionUtil.imageExtractedFromVideoPath),
                                        S3TransferUtil.TYPE_VIDEO)
                            }

                            override fun onFailure(message: String?) {
                            }

                            override fun onProgress(message: String?) {
                            }

                            override fun onStart() {
                            }

                        })
                    }

                    override fun onFinish() {
                    }
                })
            } else if (Functions.isAudioFile(mediaUpload!!)) {
                // No compress, just upload
                typeUpload = S3TransferUtil.TYPE_AUDIO
                uploadMedia(arrayListOf(mediaUpload!!), S3TransferUtil.TYPE_AUDIO)
            }
            initViewUpload()
        }
    }

    fun uploadMedia(lsPath: List<String>, type: Int) {
//        progressDialog?.setMessage("Uploading...")
//        progressDialog?.show()
        //initViewUpload()
        s3TransferUtil.beginUploads(
                lsPath, type, object : S3TransferUtil.TrackingUpload {

            override fun onProgressChanged(file: File?, id: Int, bytesCurrent: Long, bytesTotal: Long) {
                if (type == S3TransferUtil.TYPE_VIDEO && file?.absolutePath == compressionUtil.imageExtractedFromVideoPath) {
                    // do nothing
                } else {
                    // Update progress of file
                    updateProgressBar(bytesCurrent, bytesTotal)
                }
            }

            override fun onFinishAll(fileNames: MutableList<String>?, type: Int) {
                // Save data to server
//                progressDialog?.dismiss()
                if (type == S3TransferUtil.TYPE_IMAGES) {
                    val chatFile = ChatMessage.ChatFile(ChatMessageContentType.IMAGE.type, fileNames?.get(0), "-1.0", fileNames?.get(0))
                    sendMessage(viewModel.qbChatDialog, null, ChatMessageContentType.IMAGE.type, Gson().toJson(chatFile), null, null, null, null)
                } else if (type == S3TransferUtil.TYPE_VIDEO) {
                    var videoUrl = ""
                    var videoThumbnailUrl = ""
                    fileNames?.forEach { it ->
                        val ext = it?.substring(it?.lastIndexOf("."))
                        if (ext == ".jpg" || ext == ".png") {
                            videoThumbnailUrl = it
                        } else {
                            videoUrl = it
                        }
                    }
                    val chatFile = ChatMessage.ChatFile(ChatMessageContentType.VIDEO.type, videoUrl, "-1.0", videoThumbnailUrl)
                    sendMessage(viewModel.qbChatDialog, null, ChatMessageContentType.VIDEO.type, Gson().toJson(chatFile), null, null, null, null)
                } else if (type == S3TransferUtil.TYPE_AUDIO) {
                    val timeDouble: Double = (timeRecording / 1000).toDouble()
                    val chatFile = ChatMessage.ChatFile(ChatMessageContentType.AUDIO.type, fileNames?.get(0), timeDouble.toString(), "")
                    sendMessage(viewModel.qbChatDialog, null, ChatMessageContentType.AUDIO.type, Gson().toJson(chatFile), null, null, null, null)
                }
                // Reset media
                mediaUpload = null
                timeRecording = 0
                initViewUpload()
            }

            override fun onFinishAt(index: Int) {
            }

        })
    }

    fun initViewUpload() {
        if (mediaUpload == null) {
            viewMediaUpload.visibility = View.GONE
        } else {
            progressUpload.isIndeterminate = true
            viewMediaUpload.visibility = View.VISIBLE
            if (typeUpload == S3TransferUtil.TYPE_IMAGES) {
                imvImageMedia.visibility = View.VISIBLE
                viewAudio.visibility = View.GONE
                BoxingMediaLoader.getInstance().displayThumbnail(imvImageMedia, mediaUpload!!, 150, 150)
            } else if (typeUpload == S3TransferUtil.TYPE_AUDIO) {
                imvImageMedia.visibility = View.GONE
                viewAudio.visibility = View.VISIBLE
                tvTimeDuration.text = Functions.stringForTime(timeRecording.toInt())
            } else if (typeUpload == S3TransferUtil.TYPE_VIDEO) {
                imvImageMedia.visibility = View.VISIBLE
                viewAudio.visibility = View.GONE
                BoxingMediaLoader.getInstance().displayThumbnail(imvImageMedia, mediaUpload!!, 150, 150)
            }
        }
    }

    fun updateProgressBar(bytesCurrent: Long, bytesTotal: Long) {
        progressUpload.isIndeterminate = false
        progressUpload.setProgress((bytesCurrent * 100 / bytesTotal).toInt())
    }
    /* Medias chat Upload */

    /*Functions: Show images*/
    lateinit var imagesPagerAdapter: ImagesPagerAdapter
    lateinit var animator: ViewsTransitionAnimator<Int>
    var itemPosition = FromTracker.NO_POSITION
    var imagePosition = 0

    fun initImagesPager() {
        imagesPagerAdapter = ImagesPagerAdapter(imagesViewPager, arrayListOf())

        imagesViewPager?.adapter = imagesPagerAdapter
        imagesViewPager?.setPageTransformer(true, DepthPageTransformer())
        imagesToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        imagesToolbar?.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })
    }

    private fun initAnimator() {
        val listTracker = object : FromTracker<Int> {
            override fun getViewById(imagePos: Int): View? {
                val holder = recyclerview.findViewHolderForLayoutPosition(itemPosition)
                return if (holder == null) null else adapter.getImageSelected(holder)
            }

            override fun getPositionById(imagePos: Int): Int {
                val hasHolder = recyclerview.findViewHolderForLayoutPosition(itemPosition) != null
                return if (!hasHolder || getViewById(imagePos) != null)
                    itemPosition
                else
                    FromTracker.NO_POSITION
            }
        }

        val pagerTracker = object : SimpleTracker() {
            public override fun getViewAt(pos: Int): View? {
                val holder = imagesPagerAdapter.getViewHolder(pos)
                return if (holder == null) null else ImagesPagerAdapter.getImage(holder)
            }
        }

        animator = GestureTransitions.from<Int>(recyclerview, listTracker).into(imagesViewPager, pagerTracker)
        animator.addPositionUpdateListener(this)
    }

    override fun onPositionUpdate(position: Float, isLeaving: Boolean) {
        imagesBackground.setVisibility(if (position == 0f) View.INVISIBLE else View.VISIBLE)
        imagesBackground.getBackground().setAlpha((255 * position).toInt())

        imagesToolbar.setVisibility(if (position == 0f) View.INVISIBLE else View.VISIBLE)
        imagesToolbar.setAlpha(position)

        if (isLeaving && position == 0f) {
            imagesPagerAdapter.setActivated(false)
        }
    }

    open fun onBackPressed() {
        if (!animator?.isLeaving()!!) {
            animator?.exit(true)
        } else {
            activity.finish()
        }
    }
    /*Functions: Show images*/

    /* Popup quick action menu */
    lateinit var quickActionChat: QuickAction
    lateinit var quickActionMessage: QuickAction
    lateinit var quickActionDelete: QuickAction
    val ID_IMAGE = 0
    val ID_VIDEO = 1
    val ID_LOCATION = 2
    val ID_CREDIT = 3
    val ID_GIFT = 4

    val ID_FORWARD = 0
    val ID_COPY = 1
    val ID_DELETE = 2
    private fun initQuickAction() {
        // Config default color
        QuickAction.setDefaultColor(ResourcesCompat.getColor(resources, R.color.colorWhite, null))
        QuickAction.setDefaultTextColor(Color.BLACK)

        val imageItem = ActionItem(ID_IMAGE, "", R.drawable.ic_chat_gallery)
        val videoItem = ActionItem(ID_VIDEO, "", R.drawable.ic_chat_camera)
        val locationItem = ActionItem(ID_LOCATION, "", R.drawable.ic_chat_location)
        val creditItem = ActionItem(ID_CREDIT, "", R.drawable.ic_chat_transfer_money)
        val giftItem = ActionItem(ID_GIFT, "", R.drawable.ic_chat_send_gift)

        val forwardItem = ActionItem(ID_FORWARD, "Forward")
        val copyItem = ActionItem(ID_COPY, "Copy")
        val deleteItem = ActionItem(ID_DELETE, "Delete")

        //use setSticky(true) to disable QuickAction dialog being dismissed after an item is clicked
        //create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout orientation
        quickActionChat = QuickAction(activity, QuickAction.HORIZONTAL)
        quickActionChat.setColorRes(R.color.colorWhite)
        //set divider with color
        quickActionChat.setDividerColor(ContextCompat.getColor(activity, R.color.colorWhite));
        //set enable divider default is disable for vertical
        quickActionChat.setEnabledDivider(true)
        //Note this must be called before addActionItem()
        //add action items into QuickAction
        quickActionChat.addActionItem(imageItem, videoItem, locationItem)
        if (viewModel.chatDialog?.value?.type == ChatDialogType.PRIVATE.type) {
            quickActionChat.addActionItem(creditItem, giftItem)
        }

        quickActionMessage = QuickAction(activity, QuickAction.HORIZONTAL)
        quickActionMessage.setColorRes(R.color.colorBlack)
        quickActionMessage.setTextColorRes(R.color.colorWhite)
        quickActionMessage.setDividerColor(ContextCompat.getColor(activity, R.color.colorWhite));
        quickActionMessage.setEnabledDivider(true);
        quickActionMessage.addActionItem(forwardItem, copyItem, deleteItem)

        quickActionChat.setOnActionItemClickListener(object : QuickAction.OnActionItemClickListener {
            override fun onItemClick(item: ActionItem?) {
                if (item?.actionId == ID_IMAGE) {
                    val singleImgConfig = BoxingConfig(BoxingConfig.Mode.SINGLE_IMG).needCamera(R.drawable.ic_boxing_camera).mediaFilter(MediaFilter(0, 0, 10))
                    Boxing.of(singleImgConfig).withIntent(activity, BoxingActivity::class.java).start(activity, CataloguePostFragment.IMAGE_REQUEST_CODE)
                } else if (item?.actionId == ID_VIDEO) {
                    val singleImgConfig = BoxingConfig(BoxingConfig.Mode.VIDEO).needCamera(R.drawable.ic_boxing_camera)
                    Boxing.of(singleImgConfig).withIntent(activity, BoxingActivity::class.java).start(activity, CataloguePostFragment.VIDEO_REQUEST_CODE)
                } else if (item?.actionId == ID_LOCATION) {
                    val rxPermissions = RxPermissions(activity)
                    rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                            .subscribe(object : io.reactivex.Observer<Boolean> {
                                override fun onNext(t: Boolean) {
                                    if (t!!) {
                                        val builder = PlacePicker.IntentBuilder();
                                        try {
                                            val intent: Intent = builder.build(activity);
                                            startActivityForResult(intent, PLACE_PICKER_REQUEST);
                                        } catch (e: GooglePlayServicesRepairableException) {
                                            e.printStackTrace();
                                        } catch (e: GooglePlayServicesNotAvailableException) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(activity, "Permission denied", Toast.LENGTH_LONG).show()
                                    }
                                }

                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onError(e: Throwable) {

                                }

                                override fun onComplete() {

                                }
                            })
                } else if (item?.actionId == ID_CREDIT) {
                    if (viewModel.walletInfo != null && viewModel.friendChat != null) {
                        startActivity(CashTransferActivity.getIntent(activity, viewModel.friendChat!!, viewModel.walletInfo?.value?.walletPassword))
                    } else {
                        Functions.showToastShortMessage(activity, "Cannot get wallet data or friend")
                    }
                } else if (item?.actionId == ID_GIFT) {
                    if (viewModel.friendChat != null) {
                        startActivity(Intent(activity, MyGiftActivity::class.java).putExtra(MyGiftFragment.ARGUMENT_FRIEND, viewModel.friendChat).putExtra(MyGiftFragment.ARGUMENT_FROM_SCREEN, MyGiftFragment.FROM_SCREEN_CHAT))
                    } else {
                        Functions.showToastShortMessage(activity, "Cannot get friend")
                    }
                }
            }

        })

        quickActionMessage.setOnActionItemClickListener(object : QuickAction.OnActionItemClickListener {
            override fun onItemClick(item: ActionItem?) {
                if (item?.actionId == ID_COPY) {
                    ChatMessageActionHolder.getInstance().type = ID_COPY
                    Functions.setClipboard(activity, if (ChatMessageActionHolder.getInstance().chatMessage.text.isNullOrBlank())
                        "" else ChatMessageActionHolder.getInstance().chatMessage.text.toString()) // Make a trick here to display paste option
                } else if (item?.actionId == ID_DELETE) {
                    Functions.showAlertDialogYesNo(activity, "", "Do you want to delete this message?", object : CallbackAlertDialogListener {
                        override fun onClickOk() {
                            ChatMessageActionHolder.getInstance().type = ID_DELETE
                            deleteMessage(ChatMessageActionHolder.getInstance().chatMessage)
                        }

                        override fun onClickCancel() {
                        }
                    })
                } else if (item?.actionId == ID_FORWARD) {
                    ChatMessageActionHolder.getInstance().type = ID_FORWARD
                    var chatUserId: Int? = null
                    if (viewModel.qbChatDialog.type == QBDialogType.PRIVATE) {
                        run loop@ {
                            viewModel.qbChatDialog.occupants.forEach {
                                if (it != viewModel.sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CHAT_USER_ID)) {
                                    chatUserId = it
                                    return@loop
                                }
                            }
                        }
                        startActivity(ForwardListActivity.getIntent(activity,
                                FriendsListActivity.FORWARD_MESSAGE, chatUserId, null))
                    } else if (viewModel.qbChatDialog.type == QBDialogType.GROUP) {
                        startActivity(ForwardListActivity.getIntent(activity,
                                FriendsListActivity.FORWARD_MESSAGE, null, viewModel.qbChatDialog.dialogId))
                    }
                }
            }
        })

        quickActionDelete = QuickAction(activity, QuickAction.HORIZONTAL)
        quickActionDelete.setColorRes(R.color.colorBlack)
        quickActionDelete.setTextColorRes(R.color.colorWhite)
        quickActionDelete.setDividerColor(ContextCompat.getColor(activity, R.color.colorWhite));
        quickActionDelete.setEnabledDivider(true);
        quickActionDelete.addActionItem(deleteItem)
        quickActionDelete.setOnActionItemClickListener(object : QuickAction.OnActionItemClickListener {
            override fun onItemClick(item: ActionItem?) {
                if (item?.actionId == ID_DELETE) {
                    Functions.showAlertDialogYesNo(activity, "", "Do you want to delete this message?", object : CallbackAlertDialogListener {
                        override fun onClickOk() {
                            ChatMessageActionHolder.getInstance().type = ID_DELETE
                            deleteMessage(ChatMessageActionHolder.getInstance().chatMessage)
                        }

                        override fun onClickCancel() {
                        }
                    })
                }
            }
        })

    }
    /* Popup quick action menu */

    /* Audio record */
    lateinit var audioRecorder: AudioRecorder
    lateinit var dialogAudioRecorder: DialogAudioRecorder
    var timeRecording: Long = 0
    fun initAudioRecorder() {
        audioRecorder = AudioRecorder.newBuilder()
                // Required
                .useInBuildFilePathGenerator(activity)
                .build()
        // Optional
        //                .setDuration(10)
        //                .setAudioSource(MediaRecorder.AudioSource.MIC)
        //                .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        //                .setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        //                .setAudioSamplingRate(44100)
        //                .setAudioChannels(CHANNEL_STEREO)
        //                .setAudioEncodingBitRate(96000)
        audioRecorder.setMediaRecordListener(QBMediaRecordListenerImpl())

        dialogAudioRecorder = DialogAudioRecorder(activity, object : DialogAudioRecorder.IDialogAudioRecorder {
            override fun onClickBtnSave(time: Long) {
                timeRecording = time
                stopRecord()
            }

            override fun onClickBtnCancel() {
                cancelRecord()
            }

        })
    }

    open fun startRecord() {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(object : io.reactivex.Observer<Boolean> {
                    override fun onNext(t: Boolean) {
                        if (t!!) {
                            Log.d(TAG, "startRecord")
                            dialogAudioRecorder.show()
                            dialogAudioRecorder.startTimer()
                            audioRecorder.startRecord()
                        } else {
                            Toast.makeText(activity, "Permission denied", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {

                    }
                })
    }

    open fun stopRecord() {
        Log.d(TAG, "stopRecord")
        audioRecorder.stopRecord()
    }

    open fun cancelRecord() {
        Log.d(TAG, "cancelRecord")
        audioRecorder.cancelRecord()
    }

    fun clearRecorder() {
        audioRecorder.releaseMediaRecorder()
    }


    private inner class QBMediaRecordListenerImpl : QBMediaRecordListener {

        override fun onMediaRecorded(file: File) {
            processSendMessage(file)
        }

        override fun onMediaRecordError(e: MediaRecorderException) {
            Log.d(TAG, "onMediaRecordError e= " + e.message)
            clearRecorder()
        }

        override fun onMediaRecordClosed() {
            //Toast.makeText(activity, "Audio is not recorded", Toast.LENGTH_LONG).show()
        }
    }

    fun processSendMessage(file: File) {
        Log.d(TAG, "Audio recorded! " + file.path)
        mediaUpload = file.path
        compressMedia()
    }
    /* Audio record */

    /* Forward, Copy, Paste, Delete chat message */
    lateinit var dialogChatCopyMessage: DialogChatCopyMessage

    fun setCopyPasteEdtChat() {
        // Init image/video/location show
        dialogChatCopyMessage = DialogChatCopyMessage(activity, object : DialogChatCopyMessage.IDialogChatCopyMessage {
            override fun onClickBtnSave(dialogChatCopyMessage: ChatMessage?) {
                ChatMessageActionHolder.getInstance().chatMessage?.let {
                    sendMessage(viewModel.qbChatDialog, if (it.text?.trim().isNullOrBlank()) null else it.text,
                            it.customParam.chat_content_type,
                            if (it.customParam.chat_file != null) Gson().toJson(it.customParam.chat_file) else null,
                            if (it.customParam.chat_location != null) Gson().toJson(it.customParam.chat_location) else null,
                            if (it.customParam.credit_transaction != null) Gson().toJson(it.customParam.credit_transaction) else null,
                            if (it.customParam.gift != null) Gson().toJson(it.customParam.gift) else null,
                            if (it.customParam.uxc_system_info != null) Gson().toJson(it.customParam.uxc_system_info) else null)
                }
            }

            override fun onClickBtnCancel() {
            }

        })

        // Set paste action
        editChatMessage.addListener(object : GoEditText.GoEditTextListener {
            override fun onUpdate() {
                ChatMessageActionHolder.getInstance().chatMessage?.let {
                    when (it.customParam.chat_content_type) {
                        ChatMessageContentType.TEXT.type -> fragmentBinding.editChatMessage.setText(it.text)
                        ChatMessageContentType.IMAGE.type -> {
                            dialogChatCopyMessage.show()
                            dialogChatCopyMessage.initValue(it)
                            fragmentBinding.editChatMessage.setText("")
                        }
                        ChatMessageContentType.VIDEO.type -> {
                        }
                        ChatMessageContentType.AUDIO.type -> {
                        }
                        ChatMessageContentType.LOCATION.type -> {
                        }
                    }
                }
            }

            override fun processConsume(consume: Boolean): Boolean {
                ChatMessageActionHolder.getInstance().chatMessage?.let {
                    when (it.customParam.chat_content_type) {
                        ChatMessageContentType.TEXT.type -> return consume
                        ChatMessageContentType.IMAGE.type -> return true

                        ChatMessageContentType.VIDEO.type -> {
                        }
                        ChatMessageContentType.AUDIO.type -> {
                        }
                        ChatMessageContentType.LOCATION.type -> {
                        }
                    }
                }
                return consume
            }


        })
    }

    fun deleteMessage(chatMessage: ChatMessage) {
        viewModel.deleteMessage(chatMessage)

        viewModel.chatMessageDelete.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                //showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                //hideProgressDialog()
                if (ChatMessageActionHolder.getInstance().chatMessage != null && ChatMessageActionHolder.getInstance().type == ID_DELETE) {
                    ChatMessageActionHolder.getInstance().chatMessage = null
                    ChatMessageActionHolder.getInstance().type = -1
                }
            } else if (it?.status == Status.ERROR) {
                showSnackbarMessage(it?.message)
            }
        })
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onForwardSelectEvent(forwardSelectEvent: ForwardSelectEvent) {
        if ((forwardSelectEvent?.list != null && forwardSelectEvent?.list.isNotEmpty()) ||
                (forwardSelectEvent?.listGroupDialogs != null && forwardSelectEvent?.listGroupDialogs.isNotEmpty())) {
            forwardMessage(forwardSelectEvent.list, forwardSelectEvent.listGroupDialogs)
        }
    }

    fun forwardMessage(list: List<Friend>?, listGroupDialogs: List<ChatDialog>?) {
        ChatMessageActionHolder.getInstance().chatMessage?.let {
            viewModel.forwardMessage(list, listGroupDialogs, it.text, it.customParam.chat_content_type,
                    if (it.customParam.chat_file != null) Gson().toJson(it.customParam.chat_file) else null,
                    if (it.customParam.chat_location != null) Gson().toJson(it.customParam.chat_location) else null)
        }
    }
    /* Forward, Copy, Paste, Delete chat message */

}