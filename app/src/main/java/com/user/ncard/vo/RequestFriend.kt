package com.user.ncard.vo

import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 11/9/17.
 */
data class RequestFriend(@SerializedName("recipient_id") val recipientId: Int)

data class FriendRequestStatus(@SerializedName("status") val status: String)

data class AllFriendRequest(@SerializedName("initiator") val initiator: User, @SerializedName("recipient") val recipient: User)

data class FriendRemark(@SerializedName("remark") val remark: String)

data class Contact(@SerializedName("mobile") val mobile: List<String?>, @SerializedName("email") val email: List<String?>)
data class ContactWithName(val name: String, @SerializedName("mobile") val mobile: List<String?>, @SerializedName("email") val email: List<String?>)