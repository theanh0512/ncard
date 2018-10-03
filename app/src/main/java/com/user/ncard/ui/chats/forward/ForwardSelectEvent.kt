package com.user.ncard.ui.chats.forward

import com.user.ncard.vo.ChatDialog
import com.user.ncard.vo.Friend

/**
 * Created by trong-android-dev on 31/10/17.
 */
data class ForwardSelectEvent(val list: List<Friend>?, val listGroupDialogs: List<ChatDialog>?) {
}