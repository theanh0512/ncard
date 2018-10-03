package com.user.ncard.api

import com.google.gson.annotations.SerializedName
import com.user.ncard.vo.User

/**
 * Created by Pham on 23/9/2017.
 */
class SearchUserApiResponse(@SerializedName("user") val user: User,
                            val status: String, val mutualFriendCount: Int)

class SearchUserByJobApiResponse(val users: List<SearchUserApiResponse>)