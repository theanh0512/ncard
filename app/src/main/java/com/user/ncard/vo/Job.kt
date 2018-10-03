package com.user.ncard.vo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 12/10/17.
 */
@Entity(primaryKeys = arrayOf("id"), tableName = "job")
data class Job(@SerializedName("id") var id: Int,
               @SerializedName("job_title") var jobTitle: String?,
               @SerializedName("industry") var industry: String?,
               @SerializedName("company_email") var companyEmail: String?,
               @SerializedName("company_name") var companyName: String?,
               @SerializedName("mobile") var mobile: String?,
               @SerializedName("tel1") var tel1: String?,
               @SerializedName("tel2") var tel2: String?,
               @SerializedName("address") var address: String?,
               @SerializedName("description") var description: String?,
               @SerializedName("country") var country: String?,
               @SerializedName("from") var from: String?,
               @SerializedName("to") var to: String?,
               @SerializedName("media") var media: ArrayList<String>?,
               @SerializedName("cert") var cert: ArrayList<String>?,
               @SerializedName("user_id") var userId: Int,
               @SerializedName("did") var did: String?,
               @SerializedName("fax") var fax: String?,
               @SerializedName("website") var website: String?,
               @ColumnInfo(name = "card_id") var cardId: Int?) : Parcelable {
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
            parcel.createStringArrayList(),
            parcel.createStringArrayList(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(jobTitle)
        parcel.writeString(industry)
        parcel.writeString(companyEmail)
        parcel.writeString(companyName)
        parcel.writeString(mobile)
        parcel.writeString(tel1)
        parcel.writeString(tel2)
        parcel.writeString(address)
        parcel.writeString(description)
        parcel.writeString(country)
        parcel.writeString(from)
        parcel.writeString(to)
        parcel.writeStringList(media)
        parcel.writeStringList(cert)
        parcel.writeInt(userId)
        parcel.writeString(did)
        parcel.writeString(fax)
        parcel.writeString(website)
        parcel.writeValue(cardId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Job> {
        override fun createFromParcel(parcel: Parcel): Job {
            return Job(parcel)
        }

        override fun newArray(size: Int): Array<Job?> {
            return arrayOfNulls(size)
        }
    }
}