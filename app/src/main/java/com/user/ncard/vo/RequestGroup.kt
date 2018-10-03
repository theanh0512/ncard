package com.user.ncard.vo

import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 6/11/17.
 */
data class RequestGroup(val name: String, val members: List<Int>?, @SerializedName("name_cards") val nameCard: List<Int>?)