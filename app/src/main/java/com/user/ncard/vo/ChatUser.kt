package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by dangui on 10/11/17.
 */

@Entity
class ChatUser(
        @PrimaryKey var userId: Int?,
        var fullName: String?,
        var email: String?,
        var login: String?,
        var phone: String?,
        var website: String?,
        var lastRequestAt: Date?,
        var createdAt: Date?,
        var updatedAt: Date?,
        var data: String? //extra custom parameters
) {
    @Ignore
    constructor(userId: Int?) : this(-1, null, null, null, null, null, null, null, null, null)
}