package com.user.ncard.ui.chats.friends

import com.user.ncard.vo.Friend

/**
 * Created by trong-android-dev on 31/10/17.
 */
data class FriendsSelectEvent(val list: List<Friend>?, val name: String?, val from: Int) {
}