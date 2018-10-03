package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * Created by dangui on 10/11/17.
 */

@Entity
class NCardInfo(
        @PrimaryKey var id: Int = 12,// just set  default
        var friendRequest: Int? = 0,
        var friendRequestSent: Int? = 0,
        var friendRecommendation: Int? = 0,
        var cardRecommendation: Int? = 0
) : Serializable {
    @Ignore
    constructor() : this(12, 0, 0, 0, 0)

    @Ignore
    constructor(friendRequest: Int? = 0,
                friendRequestSent: Int? = 0,
                friendRecommendation: Int? = 0,
                cardRecommendation: Int? = 0) : this(12, if(friendRequest != null) friendRequest else 0,
            if(friendRequestSent != null) friendRequestSent else 0, if(friendRecommendation != null) friendRecommendation else 0,
            if(cardRecommendation != null) cardRecommendation else 0)
}