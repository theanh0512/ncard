package com.user.ncard.vo

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey

/**
 * Created by trong-android-dev on 16/10/17.
 */
@Entity(primaryKeys = arrayOf("id", "ownerId", "postId"),
        foreignKeys = arrayOf(
                ForeignKey(entity = CataloguePost::class, parentColumns = arrayOf("id"), childColumns = arrayOf("postId"),
                    onDelete = ForeignKey.CASCADE)))
class CatalogueComment(
        var id: Int,
        var ownerId: Int,
        var ownerName: String?,
        var ownerImageUrl: String?,
        var content: String,
        var createdAt: String,
        @ColumnInfo(name = "postId") var postId: Int)
