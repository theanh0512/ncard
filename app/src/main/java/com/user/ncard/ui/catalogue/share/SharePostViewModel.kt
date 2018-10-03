package com.user.ncard.ui.catalogue.share

import com.user.ncard.ui.catalogue.BaseViewModel
import javax.inject.Inject

/**
 * Created by trong-android-dev on 20/10/17.
 */
class SharePostViewModel @Inject constructor() : BaseViewModel() {

    lateinit var visibility: String

    override fun initData() {

    }

    fun init(visibility: String) {
        this.visibility = visibility
    }


}