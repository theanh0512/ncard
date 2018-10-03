package com.user.ncard.vo

import android.arch.persistence.room.Entity

/**
 * Created by trong-android-dev on 3/11/2017.
 * Tracking screens: All cataloguePost: (0, all); My cataloguePost(1, me), User cataloguePost(userId, userName)
 */
@Entity(primaryKeys = arrayOf("id"))
data class CatalogueScreenTracking(
        val id: Int,
        val name: String
)
