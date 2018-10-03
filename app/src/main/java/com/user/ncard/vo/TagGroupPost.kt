package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by trong-android-dev on 16/10/17.
 */
@Entity(primaryKeys = arrayOf("id"))
class TagGroupPost(var id: Int, var name: String) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TagGroupPost> {
        override fun createFromParcel(parcel: Parcel): TagGroupPost {
            return TagGroupPost(parcel)
        }

        override fun newArray(size: Int): Array<TagGroupPost?> {
            return arrayOfNulls(size)
        }
    }

}