package com.user.ncard.vo

import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 20/12/17.
 */
data class EphemeralKey(@SerializedName("associated_objects") val associatedObject: List<AssociatedObject>,
                        val id: String, @SerializedName("object") val type: String, val created: String,
                        val expires: String, @SerializedName("livemode") val liveMode: Boolean, val secret: String)

data class AssociatedObject(val type: String, val id: String)