package com.user.ncard.ui.catalogue.main

/**
 * Created by trong-android-dev on 30/10/17.
 */
abstract class Media(var path: String) {

    enum class TYPE {
        IMAGE, VIDEO, AUDIO
    }

    abstract fun getType(): TYPE

}