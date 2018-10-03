package com.user.ncard.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Pham on 11/9/17.
 */
data class UserFilter(@SerializedName("name") val name: String,
                      @SerializedName("industry") val industry: ArrayList<String>,
                      @SerializedName("nationality") val nationality: ArrayList<String>,
                      @SerializedName("country") val country: ArrayList<String>,
                      @SerializedName("gender") val gender: ArrayList<String>) : Serializable

data class ProfessionFiler(val keyword: String)