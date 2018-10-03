package com.user.ncard.vo

/**
 * Created by trong-android-dev on 16/10/17.
 */
class SharePost(var id: Int, var name: String, var des: String, var selected: Boolean) {

    enum class SharePostType(val type : String) {
        ALL("All"),
        PUBLIC("Public"),
        FRIENDS("Friends");
    }
}