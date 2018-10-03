package com.user.ncard.ui.chats.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.vo.ChatMessage
import com.user.ncard.vo.TransferCreditResponse
import kotlinx.android.synthetic.main.view_dialog_accept_transfer.*

/**
 * Created by trong-android-dev on 8/12/17.
 */
class DialogAcceptTransferCredit(context: Context) : Dialog(context) {

    constructor(context: Context, listener: IDialogAcceptTransferCredit) : this(context) {
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

        setContentView(R.layout.view_dialog_accept_transfer)
        setCanceledOnTouchOutside(true)
        getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        btnSave?.setOnClickListener({
            listener.onClickBtnSave(message, credit_transaction)
            dismiss()
        })

        btnCancel?.setOnClickListener({
            listener.onClickBtnCancel(message, credit_transaction)
            dismiss()
        })
    }

    var message: ChatMessage? = null
    var credit_transaction: TransferCreditResponse? = null

    interface IDialogAcceptTransferCredit {
        fun onClickBtnSave(message: ChatMessage?, credit_transaction: TransferCreditResponse?)
        fun onClickBtnCancel(message: ChatMessage?, credit_transaction: TransferCreditResponse?)
    }

    open fun start(message: ChatMessage?, credit_transaction: TransferCreditResponse?, money: String?, status: String?, des: String?) {
        this.message = message
        this.credit_transaction = credit_transaction

        tvMoney.text = if (money.isNullOrBlank()) "" else money
        tvStatus.text = if (money.isNullOrBlank()) "" else status
        tvDes.text = if (money.isNullOrBlank()) "" else des
        tvStatus.visibility = View.GONE

        show()
    }


}