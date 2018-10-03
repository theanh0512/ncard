package com.user.ncard.ui.catalogue.main

/**
 * Created by trong-android-dev on 30/10/17.
 */
class Video(path: String, val videoUrl: String): Media(path) {

    override fun getType(): TYPE {
        return TYPE.VIDEO
    }

}