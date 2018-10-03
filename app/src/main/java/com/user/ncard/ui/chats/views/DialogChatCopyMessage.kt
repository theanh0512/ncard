package com.user.ncard.ui.chats.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.load.engine.GlideException
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.vo.ChatMessage
import com.user.ncard.vo.ChatMessageContentType
import kotlinx.android.synthetic.main.view_dialog_chat_message_copy.*

/**
 * Created by trong-android-dev on 8/12/17.
 */
class DialogChatCopyMessage(context: Context) : Dialog(context) {

    constructor(context: Context, listener: IDialogChatCopyMessage) : this(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Functions.hideSoftKeyboard(context as Activity)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        // update dialog width
        val lp = WindowManager.LayoutParams()
        val window = window
        lp.copyFrom(window!!.attributes)
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp

        setContentView(R.layout.view_dialog_chat_message_copy)
        setCanceledOnTouchOutside(false)
        getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        /*btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        imvImage = findViewById(R.id.imvImage)
        progressBar = findViewById(R.id.progressBar)
        imvVideoPlay = findViewById(R.id.imvImage)*/

        btnSave?.setOnClickListener({
            listener?.onClickBtnSave(chatMessage)
            dismiss()
        })

        btnCancel?.setOnClickListener({
            listener?.onClickBtnCancel()
            dismiss()
        })
    }


    /*var btnSave: Button? = null
    var btnCancel: Button? = null
    var imvImage: ImageView? = null
    var progressBar: ProgressBar? = null
    var imvVideoPlay: ImageView? = null*/

    var chatMessage: ChatMessage? = null

    interface IDialogChatCopyMessage {
        fun onClickBtnSave(chatMessage: ChatMessage?)
        fun onClickBtnCancel()
    }

    fun initValue(chatMessage: ChatMessage) {
        imvVideoPlay?.visibility = View.GONE
        this.chatMessage = chatMessage
        if (chatMessage.customParam.chat_content_type == ChatMessageContentType.IMAGE.type) {
            loadImage(imvImage!!, progressBar!!, chatMessage.customParam.chat_file?.thumbnailRemoteUrl)
        } else if (chatMessage.customParam.chat_content_type == ChatMessageContentType.LOCATION.type) {
            loadImage(imvImage!!, progressBar!!, Functions.getGoogleMapStatic(chatMessage?.customParam?.chat_location?.lat.toString(), chatMessage?.customParam?.chat_location?.lng.toString(), context))
        } else if (chatMessage.customParam.chat_content_type == ChatMessageContentType.VIDEO.type) {
            loadImage(imvImage!!, progressBar!!, chatMessage.customParam.chat_file?.thumbnailRemoteUrl)
            imvVideoPlay?.visibility = View.VISIBLE
        }
    }

    fun loadImage(imageView: ImageView, progressBar: ProgressBar, url: String?) {
        progressBar.animate().setStartDelay(10).alpha(1f)
        // Loading image
        GlideHelper.displayRawCenterCrop(imageView, url, 0, 0, object : GlideHelper.ImageLoadingListener {
            override fun onLoaded() {
                progressBar.animate().cancel()
                progressBar.animate().alpha(0f)
            }

            override fun onFailed(e: GlideException) {
                progressBar.animate().alpha(0f)
            }
        })
    }


}