package com.user.ncard.ui.chats.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.SystemClock
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.Functions

/**
 * Created by trong-android-dev on 8/12/17.
 */
class DialogAudioRecorder(context: Context) : Dialog(context) {

    constructor(context: Context, listener: IDialogAudioRecorder) : this(context) {
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

        setContentView(R.layout.view_dialog_audio_recorder)
        setCanceledOnTouchOutside(false)
        getWindow()!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        recordChronometer = findViewById(R.id.tvTime)

        btnSave?.setOnClickListener({
            recordChronometer?.stop()
            val elapseTime: Long = SystemClock.elapsedRealtime() - recordChronometer!!.base
            listener?.onClickBtnSave(elapseTime)
            dismiss()
        })

        btnCancel?.setOnClickListener({
            recordChronometer?.stop()
            listener?.onClickBtnCancel()
            dismiss()
        })
    }


    var btnSave: Button? = null
    var btnCancel: Button? = null
    var recordChronometer: Chronometer? = null

    interface IDialogAudioRecorder {
        fun onClickBtnSave(time: Long)
        fun onClickBtnCancel()
    }

    open fun startTimer() {
        recordChronometer?.setBase(SystemClock.elapsedRealtime())
        recordChronometer?.start()
    }


}