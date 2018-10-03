package com.user.ncard.ui.catalogue

import com.user.ncard.vo.TagPost

/**
 * Created by trong-android-dev on 31/10/17.
 */
data class CatalogueFilterEvent(var visibility: String?, var tags: List<String>?, var postCategory: String?,
                                var keyword: String?, var year: Int?) {
    constructor() : this("", null, null, null, -1)

    fun toStringfiter(): String {
        var result = ""
        if (tags != null && tags?.isNotEmpty()!!) {
            tags?.forEach {
                result = result.plus(it).plus(",")
            }
            result = result.removeSuffix(",")
        }
        return result
    }
}