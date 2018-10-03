package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import java.io.Serializable

/**
 * Created by Trong on 30/11/2017.
 */
@Entity(primaryKeys = arrayOf("id"))
data class BroadcastGroup(
        val id: Int,
        val name: String,
        val memberIds: List<Int>?,
        var members: List<Member>?
) :Serializable{

    @Ignore constructor() : this(-1, "", ArrayList<Int>(), ArrayList<Member>())

    class Member(var id: Int, var username: String?, var firstName: String?,
                 var lastName: String?, var email: String?, var profileImageUrl: String?, var thumbnailUrl: String?) : Serializable
}