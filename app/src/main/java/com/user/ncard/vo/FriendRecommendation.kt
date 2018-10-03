package com.user.ncard.vo

/**
 * Created by Pham on 11/9/17.
 */
data class FriendRecommendation(val suggestedFriend: SuggestedFriend, val from: From, val friendSharingId: Int)

data class SuggestedFriend(val id: Int, val name: String, val profileImageUrl: String)

data class From(val id: Int, val name: String, val profileImageUrl: String)