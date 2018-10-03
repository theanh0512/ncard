package com.user.ncard.ui.catalogue.utils

import com.user.ncard.vo.TagGroupPost
import com.user.ncard.vo.TagPost

/**
 * Created by trong-android-dev on 16/10/17.
 */
data class Pagination(var count: Int, var currentPage: Int, var nextPage: Int) {
    constructor() : this(0, 0, 0)
}
