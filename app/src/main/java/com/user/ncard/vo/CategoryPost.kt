package com.user.ncard.vo

/**
 * Created by trong-android-dev on 16/10/17.
 */
class CategoryPost(var id: Int, var name: String, var des: String, var selected: Boolean) {

    enum class CategoryPostType(val type : String) {
        BUSINESS("Business"),
        PERSONAL("Personal");
    }
}