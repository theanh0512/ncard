package com.user.ncard.ui.catalogue.category

import com.user.ncard.ui.catalogue.BaseViewModel
import javax.inject.Inject

/**
 * Created by trong-android-dev on 20/10/17.
 */
class CategoryPostViewModel @Inject constructor() : BaseViewModel() {

    lateinit var category: String

    override fun initData() {

    }

    fun init(category: String) {
        this.category = category
    }


}