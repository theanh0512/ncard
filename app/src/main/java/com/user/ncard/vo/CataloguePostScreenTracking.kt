package com.user.ncard.vo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey

/**
 * Created by trong-android-dev on 3/11/17.
 * Middle table to store post and screen
 */
@Entity(primaryKeys = arrayOf("postId", "screenTrackingId")
        /*foreignKeys = arrayOf(
                ForeignKey(entity = CataloguePost::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("postId"),
                        onDelete = ForeignKey.NO_ACTION),
                ForeignKey(entity = CatalogueScreenTracking::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("screenTrackingId"),
                        onDelete = ForeignKey.NO_ACTION))*/
)
class CataloguePostScreenTracking(
        @ColumnInfo(name = "postId") var postId: Int,
        @ColumnInfo(name = "screenTrackingId") var screenTrackingId: Int)