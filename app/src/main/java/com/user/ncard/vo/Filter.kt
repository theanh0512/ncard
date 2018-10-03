package com.user.ncard.vo

import android.arch.persistence.room.Entity

/**
 * Created by Pham on 14/10/2017.
 */
@Entity(primaryKeys = arrayOf("name", "type"))
data class Filter(
        val name: String,
        val type: String,
        val parent: String
)