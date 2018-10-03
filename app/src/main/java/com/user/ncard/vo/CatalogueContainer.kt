package com.user.ncard.vo

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Relation
import com.user.ncard.ui.catalogue.main.Media
import com.user.ncard.vo.CataloguePost.Owner

/**
 * Created by trong-android-dev on 16/10/17.
 */
class CatalogueContainer(
        @Embedded var cataloguePost: CataloguePost,

        @Relation(parentColumn = "id", entityColumn = "postId") var likes: List<CatalogueLike>,
        @Relation(parentColumn = "id", entityColumn = "postId") var comments: List<CatalogueComment>,
        @Ignore var medias: List<Media>?){

    constructor() : this(CataloguePost(), ArrayList<CatalogueLike>(), ArrayList<CatalogueComment>(), null)
}