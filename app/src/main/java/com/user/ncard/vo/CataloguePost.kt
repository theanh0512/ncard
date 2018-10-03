package com.user.ncard.vo

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore

/**
 * Created by trong-android-dev on 16/10/17.
 */
@Entity(primaryKeys = arrayOf("id"))
class CataloguePost(
        var id: Int,
        var text: String,
        var photoUrls: List<String>,
        var videoUrl: String?,
        var videoThumbnailUrl: String,
        var type: String,
        var createdAt: String,
        var updatedAt: String,
        var visibility: String,
        var postCategory: String,
        @Embedded(prefix = "owner_") var owner: Owner,
        var tags: List<Tag>,
        @Ignore var likes: List<CatalogueLike>,
        @Ignore var comments: List<CatalogueComment>) {

    constructor() : this(-1, "",
            ArrayList<String>(), "", "", "", "", "", "", "",
            Owner(-1, "", ""),
            ArrayList<Tag>(), ArrayList<CatalogueLike>(), ArrayList<CatalogueComment>())

    class Owner(var id: Int, var name: String, var profileImageUrl: String?)

    class Tag(var id: Int, var name: String, var tagGroupId: Int)

}