
package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = arrayOf("id"), tableName = "group_item")
data class Group(@SerializedName("id") var id: Int,
                 @SerializedName("name") var name: String,
                 @SerializedName("members") var members: List<GroupItem?>?,
                 @SerializedName("name_cards") var nameCards: List<GroupItem?>?) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.createTypedArrayList(GroupItem),
            parcel.createTypedArrayList(GroupItem))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeTypedList(members)
        parcel.writeTypedList(nameCards)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Group> {
        override fun createFromParcel(parcel: Parcel): Group {
            return Group(parcel)
        }

        override fun newArray(size: Int): Array<Group?> {
            return arrayOfNulls(size)
        }
    }
}