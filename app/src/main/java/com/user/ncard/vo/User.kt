/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = arrayOf("id"))
data class User(@SerializedName("id") override val id: Int,
                @SerializedName("email") override val email: String?,
                @SerializedName("username") override val username: String?,
                @SerializedName("phone_number") override var phoneNumber: String?,
                @SerializedName("first_name") override var firstName: String?,
                @SerializedName("last_name") override var lastName: String?,
                @SerializedName("profile_image_url") override var profileImageUrl: String?,
                @SerializedName("thumbnail_url") val thumbnailUrl: String?,
                @SerializedName("summary") val summary: String?,
                @SerializedName("country") override var country: String?,
                @SerializedName("city") val city: String?,
                @SerializedName("address") val address: String?,
                @SerializedName("postcode") val postcode: String?,
                @SerializedName("gender") override var gender: String?,
                @SerializedName("nationality") override var nationality: String?,
                @SerializedName("status") override var status: String?,
                @SerializedName("birthday") override var birthday: String?,
                @SerializedName("remark") override var remark: String?,
                @SerializedName("created_at") var createdAt: String?,
                @SerializedName("updated_at") var updatedAt: String?,
                @SerializedName("qualification") override var qualification: String?,
                @SerializedName("chat_id") override var chatId: Int?,
                @SerializedName("chat_password") override var chatPassword: String?,
                @SerializedName("mutualFriendCount") var mutualFriendCount: Int?,
                var nameInContact: String?) : Parcelable, BaseEntity {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(email)
        parcel.writeString(username)
        parcel.writeString(phoneNumber)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(profileImageUrl)
        parcel.writeString(thumbnailUrl)
        parcel.writeString(summary)
        parcel.writeString(country)
        parcel.writeString(city)
        parcel.writeString(address)
        parcel.writeString(postcode)
        parcel.writeString(gender)
        parcel.writeString(nationality)
        parcel.writeString(status)
        parcel.writeString(birthday)
        parcel.writeString(remark)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeString(qualification)
        parcel.writeValue(chatId)
        parcel.writeString(chatPassword)
        parcel.writeValue(mutualFriendCount)
        parcel.writeString(nameInContact)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}