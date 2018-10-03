package com.user.ncard.ui.card.namecard

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.user.ncard.util.Constants

/**
 * Created by Pham on 2/11/17.
 */
class ImageSource(val uri: Uri?, val url: String, val resourceId: Int = Constants.BIGGEST_INT) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Uri::class.java.classLoader),
            parcel.readString(), parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(url)
        parcel.writeInt(resourceId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageSource> {
        override fun createFromParcel(parcel: Parcel): ImageSource {
            return ImageSource(parcel)
        }

        override fun newArray(size: Int): Array<ImageSource?> {
            return arrayOfNulls(size)
        }
    }
}