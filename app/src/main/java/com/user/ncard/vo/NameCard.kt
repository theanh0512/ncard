package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = arrayOf("id"))
data class NameCard(@SerializedName("id") var id: Int,
                    @SerializedName("name") var name: String?,
                    @SerializedName("title") var title: String?,
                    @SerializedName("role") var role: String?,
                    @SerializedName("email") var email: String?,
                    @SerializedName("company") var company: String?,
                    @SerializedName("mobile") var mobile: String?,
                    @SerializedName("address") var address: String?,
                    @SerializedName("urls") var urls: ArrayList<String>? = null,
                    @SerializedName("social") var social: String?,
                    @SerializedName("im") var im: String?,
                    @SerializedName("company_logo_url") var companyLogoUrl: String?,
                    @SerializedName("background_url") var backgroundUrl: String?,
                    @SerializedName("description") var description: String?,
                    @SerializedName("country") var country: String?,
                    @SerializedName("nationality") var nationality: String?,
                    @SerializedName("industry") var industry: String?,
                    @SerializedName("gender") var gender: String?,
                    @SerializedName("birthday") var birthday: String?,
                    @SerializedName("media_urls") var mediaUrls: ArrayList<String>?,
                    @SerializedName("cert_urls") var certUrls: ArrayList<String>?,
                    @SerializedName("tel1") var tel1: String?,
                    @SerializedName("tel2") var tel2: String?,
                    @SerializedName("did") var did: String?,
                    @SerializedName("qualification") var qualification: String?,
                    @SerializedName("website") var website: String?,
                    @SerializedName("fax") var fax: String?,
                    @SerializedName("remark") var remark: String?,
                    var isChecked: Boolean?,
                    @SerializedName("name_card_photo_front_url") var frontUrl: String?,
                    @SerializedName("name_card_photo_back_url") var backUrl: String?,
                    @SerializedName("job_id") var jobId: Int?,
                    @SerializedName("user_id") var userId: Int?,
                    @SerializedName("created_at") var createdAt: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createStringArrayList(),
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
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(title)
        parcel.writeString(role)
        parcel.writeString(email)
        parcel.writeString(company)
        parcel.writeString(mobile)
        parcel.writeString(address)
        parcel.writeStringList(urls)
        parcel.writeString(social)
        parcel.writeString(im)
        parcel.writeString(companyLogoUrl)
        parcel.writeString(backgroundUrl)
        parcel.writeString(description)
        parcel.writeString(country)
        parcel.writeString(nationality)
        parcel.writeString(industry)
        parcel.writeString(gender)
        parcel.writeString(birthday)
        parcel.writeStringList(mediaUrls)
        parcel.writeStringList(certUrls)
        parcel.writeString(tel1)
        parcel.writeString(tel2)
        parcel.writeString(did)
        parcel.writeString(qualification)
        parcel.writeString(website)
        parcel.writeString(fax)
        parcel.writeString(remark)
        parcel.writeValue(isChecked)
        parcel.writeString(frontUrl)
        parcel.writeString(backUrl)
        parcel.writeValue(jobId)
        parcel.writeValue(userId)
        parcel.writeString(createdAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NameCard> {
        override fun createFromParcel(parcel: Parcel): NameCard {
            return NameCard(parcel)
        }

        override fun newArray(size: Int): Array<NameCard?> {
            return arrayOfNulls(size)
        }
    }

    val hideJobStatus: Boolean
        get() = this.role.isNullOrEmpty() || this.company.isNullOrEmpty()
}