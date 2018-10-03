package com.user.ncard.ui.chats.broadcastchat

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
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.R
import com.user.ncard.databinding.FragmentBroadcastChatBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.card.catalogue.post.CataloguePostFragment
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.Constants
import com.user.ncard.ui.catalogue.S3TransferUtil
import com.user.ncard.ui.catalogue.main.Image
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.ui.catalogue.mediaviewer.FullScreenVideoPlayerActivity
import com.user.ncard.ui.catalogue.mediaviewer.ImagesPagerAdapter
import com.user.ncard.ui.catalogue.utils.CompressionUtil
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.detail.MessageListAdapter
import com.user.ncard.ui.chats.utils.audiorecord.AudioRecorder
import com.user.ncard.ui.chats.utils.audiorecord.MediaRecorderException
import com.user.ncard.ui.chats.utils.audiorecord.QBMediaRecordListener
import com.user.ncard.ui.chats.views.DialogAudioRecorder
import com.user.ncard.util.ChatHelper
import com.user.ncard.vo.BroadcastGroup
import com.user.ncard.vo.ChatMessage
import com.user.ncard.vo.ChatMessageContentType
import com.user.ncard.vo.Status
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_broadcast_chat.*
import kotlinx.android.synthetic.main.layout_images_pager.*
import kotlinx.android.synthetic.main.view_chat_media_upload.*
import me.piruin.quickaction.ActionItem
import me.piruin.quickaction.QuickAction
import me.shaohui.advancedluban.OnMultiCompressListener
import java.io.File
import javax.inject.Inject

/**
 * Created by dangui on 13/11/17.
 */
class BroadcastChatFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener, Injectable, ViewPositionAnimator.PositionUpdateListener, MessageListAdapter.MessageClickCallback {

    private val TAG = "BroadcastChatFragment"
    val PLACE_PICKER_REQUEST = 1

    companion object {
        fun newInstance(): BroadcastChatFragment = BroadcastChatFragment()
    }

    lateinit var viewModel: BroadcastChatViewModel
    lateinit var fragmentBinding: FragmentBroadcastChatBinding
    lateinit var adapter: MessageListAdapter

    @Inject
    lateinit var chatHelper: ChatHelper
    @Inject lateinit var ffmpeg: FFmpeg
    lateinit var compressionUtil: CompressionUtil

    lateinit var broadcastGroup: BroadcastGroup
    lateinit var s3TransferUtil: S3TransferUtil

    override fun getLayout(): Int {
        return R.layout.fragment_broadcast_chat
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_broadcast), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(BroadcastChatViewModel::class.java)
        fragmentBinding = FragmentBroadcastChatBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        broadcastGroup = bundle.getSerializable("broadcastGroup") as BroadcastGroup
        viewModel.initData(broadcastGroup)
        // viewModel.initData()
    }

    override fun init() {
        s3TransferUtil = S3TransferUtil(activity, Constants.FOLDER_CHAT)
        compressionUtil = CompressionUtil(context, ffmpeg)
        // Load data here
        initObser()
        initAdapter()
        iniSwipeRefreshLayout()
        initImagesPager()
        initAnimator()
        initQuickAction()
        initAudioRecorder()

        /*val loader = BoxingFrescoLoader(activity)
        BoxingMediaLoader.getInstance().init(loader)*/

        // Init toolbar again

        fragmentBinding.srl.post {
            fragmentBinding.srl.isRefreshing = true
            viewModel.start.value = true
        }

        fragmentBinding.buttonChatSend.setOnClickListener {
            val textToSend = fragmentBinding.editChatMessage.text.toString()
            if (textToSend.isNotBlank()) {
                sendMessage(broadcastGroup, textToSend, ChatMessageContentType.TEXT.type, null, null)
            }

            fragmentBinding.editChatMessage.setText("")
        }

        fragmentBinding.buttonAudio.setOnClickListener({
            startRecord()
        })
        fragmentBinding.buttonMenuAdd.setOnClickListener({
            quickActionChat.show(fragmentBinding.buttonMenuAdd)
        })
    }

    /* Send message */
    fun sendMessage(broadcastGroup: BroadcastGroup, text: String?, type: String, chat_file: String?, chat_location: String?) {
        viewModel.sendChatMessage(broadcastGroup, text, type, chat_file, chat_location)
    }

    fun initAdapter() {
        recyclerview.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        recyclerview.setHasFixedSize(true)
        adapter = MessageListAdapter(activity, viewModel.getItems(),
                null, chatHelper, this, viewModel.sharedPreferenceHelper)

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

        viewModel.userNotFriends.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                //showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                //hideProgressDialog()
                viewModel.processUserNotFriends()
                viewModel.prepareListPrivateChatDialogs()
            } else if (it?.status == Status.ERROR) {
                viewModel.prepareListPrivateChatDialogs()
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
                Functions.showLogMessage("Trong", "Broadcast success with " + it?.id)
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
        scrollToBottom()
        TViewUtil.EmptyViewBuilder.getInstance(activity)
                .bindView(recyclerview)
    }

    override fun onRefresh() {
        viewModel.refresh()
    }


    private fun scrollToBottom() {
        if (adapter.items.size > 1) {
            recyclerview.scrollToPosition(adapter.items.size - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                val place: Place = PlacePicker.getPlace(activity, data)
                //Functions.showToastShortMessage(activity, place.getName().toString() + " | " + place.getAddress())
                val chatLocation = ChatMessage.ChatLocation(place?.latLng?.latitude!!, place?.latLng?.longitude!!, place.name.toString(), place.address.toString())
                sendMessage(broadcastGroup, null, ChatMessageContentType.LOCATION.type, null, Gson().toJson(chatLocation))
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
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onLongClick(message: ChatMessage, position: Int) {
        /* val holder = recyclerview.findViewHolderForLayoutPosition(position)
         if (holder != null) {
             val bubbleChatSelected = adapter.getBubleChatLongClick(holder)
             bubbleChatSelected?.let {
                 quickActionMessage.show(bubbleChatSelected)
             }
         }*/
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

    override fun onAudioClick(item: ChatMessage, position: Int) {
    }

    override fun onCreditClick(item: ChatMessage, position: Int) {
    }

    override fun onGiftClick(item: ChatMessage, position: Int) {
    }

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
                    sendMessage(broadcastGroup, null, ChatMessageContentType.IMAGE.type, Gson().toJson(chatFile), null)
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
                    sendMessage(broadcastGroup, null, ChatMessageContentType.VIDEO.type, Gson().toJson(chatFile), null)
                } else if (type == S3TransferUtil.TYPE_AUDIO) {
                    val timeDouble: Double = (timeRecording / 1000).toDouble()
                    val chatFile = ChatMessage.ChatFile(ChatMessageContentType.AUDIO.type, fileNames?.get(0), timeDouble.toString(), "")
                    sendMessage(broadcastGroup, null, ChatMessageContentType.AUDIO.type, Gson().toJson(chatFile), null)
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
    val ID_IMAGE = 0
    val ID_VIDEO = 1
    val ID_LOCATION = 2
    private fun initQuickAction() {
        // Config default color
        QuickAction.setDefaultColor(ResourcesCompat.getColor(resources, R.color.colorWhite, null))
        QuickAction.setDefaultTextColor(Color.BLACK)

        val imageItem = ActionItem(ID_IMAGE, "", R.drawable.ic_chat_gallery)
        val videoItem = ActionItem(ID_VIDEO, "", R.drawable.ic_chat_camera)
        val locationItem = ActionItem(ID_LOCATION, "", R.drawable.ic_chat_location)

        val forwardItem = ActionItem(ID_VIDEO, "Forward")
        val copyItem = ActionItem(ID_LOCATION, "Copy")
        val deleteItem = ActionItem(ID_LOCATION, "Delete")

        //use setSticky(true) to disable QuickAction dialog being dismissed after an item is clicked
        //create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout orientation
        quickActionChat = QuickAction(activity, QuickAction.HORIZONTAL)
        quickActionChat.setColorRes(R.color.colorWhite)
        //set divider with color
        quickActionChat.setDividerColor(ContextCompat.getColor(activity, R.color.colorWhite));
        //set enable divider default is disable for vertical
        quickActionChat.setEnabledDivider(true);
        //Note this must be called before addActionItem()
        //add action items into QuickAction
        quickActionChat.addActionItem(imageItem, videoItem, locationItem)

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

}