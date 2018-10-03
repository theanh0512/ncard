package com.user.ncard.ui.catalogue

/**
 * Created by trong-android-dev on 16/10/17.
 */
data class RequestCatalogue(var text: String, var visibility: String, var tags: List<String>?, var postCategory: String, var photoUrls: List<String>?,
                            var videoUrl: String?, var videoThumbnailUrl: String?) {
    constructor() : this("", "", ArrayList<String>(), "", ArrayList<String>(), "", "")

    fun toLowerCaseReuqest(): RequestCatalogue {
        visibility = visibility?.toLowerCase()
        postCategory = postCategory?.toLowerCase()
        if (visibility == "friends") {
            // remove s from Friends. :|
            visibility = "friend"
        }
        return this
    }
}