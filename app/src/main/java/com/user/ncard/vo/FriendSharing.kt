package com.user.ncard.vo

import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 11/9/17.
 */
data class FriendSharing(@SerializedName("to") val to: Int, @SerializedName("friend_id") val friendId: Int)

data class NameCardSharing(@SerializedName("to") val to: Int, @SerializedName("name_card_id") val nameCardId: Int)