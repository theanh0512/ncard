package com.user.ncard.ui.catalogue.main

/**
 * Created by trong-android-dev on 30/10/17.
 */
class Audio(path: String) : Media(path) {

    override fun getType(): TYPE {
        return TYPE.AUDIO
    }

}