package com.user.ncard.ui.catalogue.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import com.user.ncard.R


/**
 * Created by trong-android-dev on 9/11/17.
 */
class ProgressLoading(var context: Context) {

    var dialog: Dialog? = null
    private var progressBar: ProgressBar? = null

    fun showProgress(message: String) {
        if (dialog == null && context != null) {
            dialog = Dialog(context)

            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setContentView(R.layout.progressbar_loading)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressBar = dialog?.findViewById(R.id.progress_bar)
            val progressText = dialog?.findViewById<View>(R.id.progress_text) as TextView
            if (message?.isNullOrEmpty()) {
                progressText.text = "" + message
                progressText.visibility = View.VISIBLE
            } else {
                progressText.visibility = View.GONE
            }
            progressBar?.visibility = View.VISIBLE
            progressBar?.isIndeterminate = true
        }

        if (dialog != null && !dialog!!.isShowing) {
            dialog?.setCancelable(false)
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.show()
        }
    }

    fun hideProgress() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }
}