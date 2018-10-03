package com.user.ncard.ui.chats.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.vo.EWalletTransactionStatusType
import kotlinx.android.synthetic.main.view_dialog_info_transfer.*

/**
 * Created by trong-android-dev on 8/12/17.
 */
class DialogInfoTransferCredit(context: Context) : Dialog(context) {

    constructor(context: Context, listener: DialogAcceptTransferCredit.IDialogAcceptTransferCredit?) : this(context) {
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

        setContentView(R.layout.view_dialog_info_transfer)
        setCanceledOnTouchOutside(true)
        getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }


    open fun start(money: String?, status: String?, des: String?) {

        if (!money.isNullOrBlank()) {
            tvMoney.text = if (money.isNullOrBlank()) "" else money
            tvMoney.visibility = View.VISIBLE
        } else {
            tvMoney.visibility = View.GONE
        }

        if (!status.isNullOrBlank()) {
            if (status == EWalletTransactionStatusType.ONHOLD.status) {
                imvStatus.setImageDrawable(context.getDrawable(R.drawable.ic_transfer_waiting))
            } else {
                imvStatus.setImageDrawable(context.getDrawable(R.drawable.ic_transfer_success))
            }
        }

        if (!des.isNullOrBlank()) {
            tvStatus.text = if (money.isNullOrBlank()) "" else des
            tvStatus.visibility = View.VISIBLE
        } else {
            tvStatus.visibility = View.GONE
        }

        tvDes.visibility = View.GONE


        show()
    }

    open fun startStatus(des: String?) {
        imvStatus.setImageDrawable(context.getDrawable(R.drawable.ic_transfer_success))
        tvDes.visibility = View.GONE
        tvMoney.visibility = View.GONE
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = des

        show()
    }


}