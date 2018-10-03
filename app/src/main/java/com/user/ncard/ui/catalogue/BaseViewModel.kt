package com.user.ncard.ui.catalogue

import android.arch.lifecycle.ViewModel

/**
 * Created by trong-android-dev on 23/10/17.
 */
abstract class BaseViewModel: ViewModel() {

    val DEFAULT_PAGE = 1

    abstract fun initData()

}