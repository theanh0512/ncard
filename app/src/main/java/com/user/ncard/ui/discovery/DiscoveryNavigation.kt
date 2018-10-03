package com.user.ncard.ui.discovery

import android.view.View

/**
 * Created by Pham on 17/9/2017.
 */
abstract class DiscoveryNavigation(val text: String, val imageRes: Int) {
    abstract fun onClick(view: View)
}