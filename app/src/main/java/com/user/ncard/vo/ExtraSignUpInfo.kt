package com.user.ncard.vo

import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 11/9/17.
 */
data class ExtraSignUpInfo(@SerializedName("first_name")val firstName:String,
                           @SerializedName("last_name")val lastName:String,
                           @SerializedName("phone_number")val phoneNumber:String)