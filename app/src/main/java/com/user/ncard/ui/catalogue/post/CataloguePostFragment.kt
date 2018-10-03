package com.user.ncard.ui.card.catalogue.post

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.bilibili.boxing.Boxing
import com.bilibili.boxing.model.config.BoxingConfig
import com.bilibili.boxing.model.config.MediaFilter
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing_impl.ui.BoxingActivity
import com.bilibili.boxing_impl.view.SpacesItemDecoration
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.google.gson.Gson
import com.tbruyelle.rxpermissions2.RxPermissions
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCataloguePostBinding
import com.user.ncard.ui.card.catalogue.share.SharePostActivity
import com.user.ncard.ui.card.catalogue.tag.TagPostActivity
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.CatalogueFilterEvent
import com.user.ncard.ui.catalogue.Constants
import com.user.ncard.ui.catalogue.S3TransferUtil
import com.user.ncard.ui.catalogue.S3TransferUtil.TYPE_IMAGES
import com.user.ncard.ui.catalogue.S3TransferUtil.TrackingUpload
import com.user.ncard.ui.catalogue.category.CategoryPostActivity
import com.user.ncard.ui.catalogue.post.CataloguePostViewModel
import com.user.ncard.ui.catalogue.post.MediaResultAdapter
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.vo.Status
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.view_catalogue_post.*
import me.shaohui.advancedluban.Luban
import me.shaohui.advancedluban.OnMultiCompressListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.inject.Inject


/**
 * Created by trong-android-dev on 16/10/17.
 */
class CataloguePostFragment : BaseFragment(), MediaResultAdapter.ClickListener {

    val TAG = "CataloguePostFragment"

    companion object {
        const val DEFAULT_SELECTED_COUNT: Int = 9
        fun newInstance(): CataloguePostFragment = CataloguePostFragment()
        val IMAGE_REQUEST_CODE = 1024
        val VIDEO_REQUEST_CODE = 2048
    }

    @Inject lateinit var ffmpeg: FFmpeg
    lateinit var viewModel: CataloguePostViewModel
    lateinit var fragmentBinding: FragmentCataloguePostBinding

    lateinit var adapter: MediaResultAdapter;
    var itemsMedia: MutableList<BaseMedia> = ArrayList<BaseMedia>()
    lateinit var mBottomSheetDialog: BottomSheetDialog
    lateinit var s3TransferUtil: S3TransferUtil

    var listenClickFilter = false

    override fun getLayout(): Int {
        return R.layout.fragment_catalogue_post
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_catalogue_create_post), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CataloguePostViewModel::class.java)
        fragmentBinding = FragmentCataloguePostBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        viewModel.initData(bundle.getString("category"), bundle.getString("share"))
    }

    override fun init() {
        // Load data here

        initAdapter()
        initObser()
        initDialogPickerMedia()
        s3TransferUtil = S3TransferUtil(activity, Constants.FOLDER_CATALOGUE)


        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(object : io.reactivex.Observer<Boolean> {
                    override fun onNext(t: Boolean) {
                        if (t!!) {
                            //mBottomSheetDialog.show()
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
        progressDialog = ProgressDialog(activity)
        progressDialog?.setTitle(null)
        progressDialog?.setCancelable(false)
        loadFFMpegBinary()
        EventBus.getDefault().register(this)
    }

    fun initDialogPickerMedia() {
        /*val loader = BoxingFrescoLoader(activity)
        BoxingMediaLoader.getInstance().init(loader)*/

        mBottomSheetDialog = BottomSheetDialog(activity)
        val sheetView = activity.layoutInflater.inflate(R.layout.view_catalogue_post_media_chooser, null)
        mBottomSheetDialog.setContentView(sheetView)


        val lnPhoto = sheetView.findViewById<View>(R.id.lnPhoto) as LinearLayout
        val lnVideo = sheetView.findViewById<View>(R.id.lnVideo) as LinearLayout

        lnPhoto.setOnClickListener({
            //val remainingImage = DEFAULT_SELECTED_COUNT - itemsMedia?.size
            val singleImgConfig = BoxingConfig(BoxingConfig.Mode.MULTI_IMG).needCamera(R.drawable.ic_boxing_camera).mediaFilter(MediaFilter(0, 0, 10))
            Boxing.of(singleImgConfig).withIntent(activity, BoxingActivity::class.java, itemsMedia as java.util.ArrayList<out BaseMedia>).start(this, IMAGE_REQUEST_CODE)
            mBottomSheetDialog.dismiss()
        })

        lnVideo.setOnClickListener({
            val singleImgConfig = BoxingConfig(BoxingConfig.Mode.VIDEO).needCamera(R.drawable.ic_boxing_camera)
            Boxing.of(singleImgConfig).withIntent(activity, BoxingActivity::class.java).start(this, VIDEO_REQUEST_CODE)
            mBottomSheetDialog.dismiss()
        })

        viewShare.setOnClickListener({
            listenClickFilter = true
            startActivity(SharePostActivity.getIntent(activity, viewModel.request.visibility))
        })

        viewTag.setOnClickListener({
            listenClickFilter = true
            startActivity(TagPostActivity.getIntent(activity, Gson().toJson(viewModel.request.tags)))
        })

        viewCategory.setOnClickListener({
            listenClickFilter = true
            startActivity(CategoryPostActivity.getIntent(activity, viewModel.request.postCategory))
        })
    }

    fun initAdapter() {
        adapter = MediaResultAdapter(activity, itemsMedia, this)
        recyclerview.setLayoutManager(GridLayoutManager(activity, 3))
        recyclerview.setAdapter(adapter)
        recyclerview.addItemDecoration(SpacesItemDecoration(8))
        recyclerview.setOnClickListener(View.OnClickListener {
            //
        })
    }

    fun initObser() {
        viewModel.catalogue.observe(this, android.arch.lifecycle.Observer {
            if (it?.status == Status.LOADING) {
                showProgressDialog()
            } else if (it?.status == Status.SUCCESS) {
                hideProgressDialog()
                // Delete cache file video and image
                Functions.deleteFile(File(Functions.getDirectory().absolutePath))
                activity.finish()
            } else if (it?.status == Status.ERROR) {
                showSnackbarMessage(it?.message)
            }
        })
    }

    fun createPostCall(photoUrls: List<String>?,
                       videoUrl: String?, videoThumbnailUrl: String?) {
        viewModel.createCataloguePost(edtMessage.text.toString().trim(), photoUrls, videoUrl, videoThumbnailUrl)
    }

    override fun clickAddMore() {
        mBottomSheetDialog.show()
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    fun onFilterEvent(filterEvent: CatalogueFilterEvent) {
        if (listenClickFilter) {
            viewModel.initFilterValues(filterEvent?.visibility, filterEvent?.tags, filterEvent?.postCategory)
        }
        listenClickFilter = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (recyclerview == null || adapter == null) {
                return
            }
            recyclerview.setVisibility(View.VISIBLE)
            val medias = Boxing.getResult(data)
            if (requestCode == IMAGE_REQUEST_CODE || requestCode == VIDEO_REQUEST_CODE) {
                itemsMedia.clear()
                itemsMedia.addAll(medias!!)
                adapter.updateShowAddMore()
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val item = menu?.findItem(R.id.menu_item_do)
        item?.setTitle(R.string.send)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_do_sth, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    var videoOutputPath = ""
    var imageExtractedFromVideoPath = ""
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_do -> {

                consume {
                    if (edtMessage.text.toString().trim().isNotEmpty() && !viewModel.category?.get().isNullOrEmpty()) {
                        if (itemsMedia?.isNotEmpty()) {
                            // Having medias chosen
                            val lsPath = itemsMedia.map { it -> it.path }

                            // COMPRESS
                            if (lsPath.size == 1 && Functions.isVideoFile(lsPath[0])) {
                                // Extract image and Compress video
                                extractImageFromVideo(lsPath[0], object : onCompress {
                                    override fun onFinish() {
                                        compressVideo(lsPath[0], object : onCompress {
                                            override fun onFinish() {
                                                // UPLOAD
                                                upload(arrayListOf(videoOutputPath, imageExtractedFromVideoPath),
                                                        S3TransferUtil.TYPE_VIDEO)
                                            }
                                        })
                                    }
                                })
                            } else {
                                // Compress images
                                compressImage(lsPath, object : OnMultiCompressListener {
                                    override fun onSuccess(fileList: MutableList<File>?) {
                                        // UPLOAD
                                        upload(fileList?.map { it.absolutePath }!!, TYPE_IMAGES)
                                    }

                                    override fun onError(e: Throwable?) {
                                    }

                                    override fun onStart() {
                                    }
                                })
                            }

                        } else {
                            // No medias chosen
                            createPostCall(null, null, null)
                        }
                    } else {
                        showToastMessage("Please input your Message or choose Category!")
                    }
                    return@consume
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    fun upload(lsPath: List<String>, type: Int) {
        progressDialog?.setMessage("Uploading...")
        progressDialog?.show()
        s3TransferUtil.beginUploads(
                lsPath, type, object : TrackingUpload {
            override fun onProgressChanged(file: File?, id: Int, bytesCurrent: Long, bytesTotal: Long) {
            }

            override fun onFinishAll(fileNames: MutableList<String>?, type: Int) {
                // Save data to server
                progressDialog?.dismiss()
                if (type == TYPE_IMAGES) {
                    createPostCall(fileNames, null, null)
                } else {
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
                    createPostCall(null, videoUrl, videoThumbnailUrl)
                }
            }

            override fun onFinishAt(index: Int) {
            }

        })
    }

    private var progressDialog: ProgressDialog? = null
    private fun loadFFMpegBinary() {
        try {
            ffmpeg?.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFailure() {
                    showUnsupportedExceptionDialog()
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            showUnsupportedExceptionDialog()
        }
    }

    /**
     * Execute FFmpeg command
     */
    private fun execFFmpegBinary(command: Array<String>, listener: onCompress) {
        try {
            ffmpeg?.execute(command, object : ExecuteBinaryResponseHandler() {
                override fun onFailure(s: String?) {
                    Log.d(TAG, "FAILED with output : " + s!!)
                    showSnackbarMessage(s)
                }

                override fun onSuccess(s: String?) {
                    Log.d(TAG, "SUCCESS with output : " + s!!)
                }

                override fun onProgress(s: String?) {
                    Log.d(TAG, "Started command : ffmpeg " + command)
                    Log.d(TAG, "progress : " + s!!)
//                    progressDialog?.setMessage("Processing\n" + s!!)
                    progressDialog?.setMessage("Processing video")
                }

                override fun onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command)
                    progressDialog?.setMessage("Processing...")
                    progressDialog?.show()
                }

                override fun onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command)
                    //Functions.saveMediaStore(activity.contentResolver, videoOutputPath, TYPE_VIDEO)
                    //Functions.saveMediaStore(activity.contentResolver, imageExtractedFromVideoPath, TYPE_IMAGES)
                    progressDialog?.dismiss()
                    listener.onFinish()
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            // do nothing for now
            Log.d(TAG, e.message)
        }

    }

    private fun showUnsupportedExceptionDialog() {
        AlertDialog.Builder(activity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { dialog, which -> activity.finish() }
                .create()
                .show()

    }

    /**
     * Compress video using FFmpeg
     */
    fun compressVideo(videoInputPath: String, listener: onCompress) {
        val videoFileInput = File(videoInputPath)
        videoOutputPath = Functions.getDirectory().absolutePath + "/" + videoFileInput.name
        val cmdFormat = "-y -i %s -r 20 -c:v libx264 -preset ultrafast -c:a copy -me_method zero -tune fastdecode -tune zerolatency -strict -2 -b:v 3000k -pix_fmt yuv420p %s"
        val command = String.format(cmdFormat, videoFileInput.absolutePath, videoOutputPath)

        val cmd = command.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (cmd.size != 0) {
            execFFmpegBinary(cmd, listener)
        } else {
            Toast.makeText(activity, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Extract first image from video using FFmpeg
     */
    fun extractImageFromVideo(videoInputPath: String, listener: onCompress) {
        val videoFileInput = File(videoInputPath)
        imageExtractedFromVideoPath = Functions.getDirectory().absolutePath + "/" + videoFileInput.name.substring(0, videoFileInput.name.lastIndexOf(".")) + ".jpg"
        val cmdFormat = "-y -i %s -ss 00:00:01.000 -vframes 1 %s"
        val command = String.format(cmdFormat, videoFileInput.absolutePath, imageExtractedFromVideoPath)

        val cmd = command.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (cmd.size != 0) {
            execFFmpegBinary(cmd, listener)
        } else {
            Toast.makeText(activity, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Compress images using Luban lib
     */
    fun compressImage(imagesList: List<String>, listener: OnMultiCompressListener) {

        val filesList = imagesList.map { File(it) }
        Luban.compress(activity, filesList)
                .setMaxSize(1000)                // limit the final image size（unit：Kb）
                .setMaxHeight(1920 * 2)             // limit image height
                .setMaxWidth(1080 * 2)              // limit image width
                .putGear(Luban.CUSTOM_GEAR)     // use CUSTOM GEAR compression mode
                .launch(object : OnMultiCompressListener {
                    override fun onSuccess(fileList: MutableList<File>?) {
                        progressDialog?.dismiss()
                        listener.onSuccess(fileList)
                    }

                    override fun onError(e: Throwable?) {
                        showSnackbarMessage(e?.message)
                        progressDialog?.dismiss()
                    }

                    override fun onStart() {
                        progressDialog?.setMessage("Processing images")
                        progressDialog?.show()
                    }
                })
    }

    interface onCompress {
        fun onFinish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}