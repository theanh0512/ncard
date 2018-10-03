package com.user.ncard.util.ext

import android.support.design.widget.Snackbar
import android.view.View

/**
 * Created by Pham on 19/9/17.
 */
fun View.showSnackbar(snackBarText: String) {
    Snackbar.make(this, snackBarText, Snackbar.LENGTH_SHORT).show()
}