package com.user.ncard.vo

import android.os.Parcelable

/**
 * Created by Pham on 2/10/17.
 */
interface BaseEntity : Parcelable {
    val username: String?
    val id: Int
    val firstName: String?
    val lastName: String?
    val email: String?
    val phoneNumber: String?
    var status: String?
    val gender: String?
    val birthday: String?
    val nationality: String?
    val country: String?
    val profileImageUrl: String?
    var remark: String?
    var chatId: Int?
    var chatPassword: String?
    val qualification: String?
}