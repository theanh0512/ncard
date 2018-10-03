package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by trong-android-dev on 16/10/17.
 */
@Entity(primaryKeys = arrayOf("id"))
class TagPost(var id: Int, var name: String, var tagGroupId: Int, var selected: Boolean) : Parcelable{

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(tagGroupId)
        parcel.writeByte(if (selected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TagPost> {
        override fun createFromParcel(parcel: Parcel): TagPost {
            return TagPost(parcel)
        }

        override fun newArray(size: Int): Array<TagPost?> {
            return arrayOfNulls(size)
        }
    }

}