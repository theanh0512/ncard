package com.user.ncard.ui.chats.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.load.engine.GlideException
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.GlideHelper
import com.user.ncard.vo.ChatMessage
import com.user.ncard.vo.ECommerceGiftStatus
import com.user.ncard.vo.SendGiftResponse
import kotlinx.android.synthetic.main.view_dialog_accept_gift.*

/**
 * Created by trong-android-dev on 8/12/17.
 */
class DialogAcceptGift(context: Context) : Dialog(context) {

    constructor(context: Context, listener: IDialogAcceptGift?) : this(context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Functions.hideSoftKeyboard(context as Activity)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        // update dialog width
        val lp = WindowManager.LayoutParams()
        val window = window
        lp.copyFrom(window!!.attributes)
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp

        setContentView(R.layout.view_dialog_accept_gift)
        setCanceledOnTouchOutside(true)
        getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        btnAccept?.setOnClickListener({
            listener?.onClickBtnAccept(chatMessage)
            dismiss()
        })

        btnCashout?.setOnClickListener({
            listener?.onClickBtnCashout(chatMessage)
            dismiss()
        })

        btnSave?.setOnClickListener({
            listener?.onClickBtnSave(chatMessage)
            dismiss()
        })
    }

    var chatMessage: ChatMessage? = null

    interface IDialogAcceptGift {
        fun onClickBtnAccept(chatMessage: ChatMessage?)
        fun onClickBtnCashout(chatMessage: ChatMessage?)
        fun onClickBtnSave(chatMessage: ChatMessage?)
    }

    open fun start(chatMessage: ChatMessage, sendGiftResponse: SendGiftResponse, sender: Boolean) {
        this.chatMessage = chatMessage
        sendGiftResponse?.let {

            tvGiftName.text = sendGiftResponse.product?.title

            tvName.text = sendGiftResponse.sender?.name.plus(": ")
            tvMessage.text = sendGiftResponse.message
            GlideHelper.displayAvatar(imvAvatar, sendGiftResponse.sender.profileImageUrl)
            loadImage(imvImage, progressBar, sendGiftResponse.product?.imageUrl)

            if (!sender && sendGiftResponse?.status == ECommerceGiftStatus.SENT.status) {
                btnCashout.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE
                btnAccept.visibility = View.VISIBLE
                tvStatus.visibility = View.GONE
            } else {
                btnCashout.visibility = View.GONE
                btnSave.visibility = View.GONE
                btnAccept.visibility = View.GONE
                tvStatus.visibility = View.VISIBLE
            }

            if (sendGiftResponse?.status == ECommerceGiftStatus.SENT.status) {
                tvStatus.text = context.getString(R.string.gift_status_awaiting)
            } else if (sendGiftResponse?.status == ECommerceGiftStatus.ACCEPTED.status) {
                tvStatus.text = context.getString(R.string.gift_status_accept)
            } else if (sendGiftResponse?.status == ECommerceGiftStatus.REJECTED.status) {
                tvStatus.text = context.getString(R.string.gift_status_reject)
            } else if (sendGiftResponse?.status == ECommerceGiftStatus.CASHED.status) {
                tvStatus.text = context.getString(R.string.gift_status_cashout)
            }

            show()
        }
    }

    open fun start(text: String, sendGiftResponse: SendGiftResponse) {
        this.chatMessage = chatMessage
        sendGiftResponse?.let {

            tvGiftName.text = sendGiftResponse.product?.title

            tvName.text = sendGiftResponse.sender?.name.plus(": ")
            tvMessage.text = sendGiftResponse.message
            GlideHelper.displayAvatar(imvAvatar, sendGiftResponse.sender.profileImageUrl)
            loadImage(imvImage, progressBar, sendGiftResponse.product?.imageUrl)

            btnCashout.visibility = View.GONE
            btnSave.visibility = View.GONE
            btnAccept.visibility = View.GONE
            tvStatus.visibility = View.VISIBLE

            tvStatus.text = text

            show()
        }
    }

    fun loadImage(imageView: ImageView, progressBar: ProgressBar, url: String?) {
        progressBar.visibility = View.GONE
        //progressBar.animate().setStartDelay(10).alpha(1f)
        // Loading image
        GlideHelper.displayRawCenterCrop(imageView, url, 0, 0, object : GlideHelper.ImageLoadingListener {
            override fun onLoaded() {
//                progressBar.animate().cancel()
//                progressBar.animate().alpha(0f)
            }

            override fun onFailed(e: GlideException) {
//                progressBar.animate().alpha(0f)
            }
        })
    }


}