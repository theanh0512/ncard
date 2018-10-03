package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = arrayOf("id"))
data class Friend(@SerializedName("id") override var id: Int,
                  @SerializedName("email") override var email: String?,
                  @SerializedName("username") override var username: String?,
                  @SerializedName("phone_number") override var phoneNumber: String?,
                  @SerializedName("first_name") override var firstName: String?,
                  @SerializedName("last_name") override var lastName: String?,
                  @SerializedName("profile_image_url") override var profileImageUrl: String?,
                  @SerializedName("thumbnail_url") var thumbnailUrl: String?,
                  @SerializedName("summary") var summary: String?,
                  @SerializedName("country") override var country: String?,
                  @SerializedName("city") var city: String?,
                  @SerializedName("address") var address: String?,
                  @SerializedName("postcode") var postcode: String?,
                  @SerializedName("gender") override var gender: String?,
                  @SerializedName("nationality") override var nationality: String?,
                  @SerializedName("birthday") override var birthday: String?,
                  @SerializedName("remark") override var remark: String?,
                  @SerializedName("created_at") var createdAt: String?,
                  @SerializedName("updated_at") var updatedAt: String?,
                  @SerializedName("qualification") override var qualification: String?,
                  @SerializedName("chat_id") override var chatId: Int?,
                  @SerializedName("chat_password") override var chatPassword: String?,
                  override var status: String?,
                  var isChecked: Boolean?,
                  @SerializedName("jobs") var jobs: List<Job?>?) : Parcelable, BaseEntity {
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
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
            parcel.createTypedArrayList(Job))

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
        parcel.writeString(birthday)
        parcel.writeString(remark)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeString(qualification)
        parcel.writeValue(chatId)
        parcel.writeString(chatPassword)
        parcel.writeString(status)
        parcel.writeValue(isChecked)
        parcel.writeTypedList(jobs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Friend> {
        override fun createFromParcel(parcel: Parcel): Friend {
            return Friend(parcel)
        }

        override fun newArray(size: Int): Array<Friend?> {
            return arrayOfNulls(size)
        }
    }
}