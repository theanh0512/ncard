package com.user.ncard.ui.chats.shipping

import com.user.ncard.vo.ChatMessage
import com.user.ncard.vo.SendGiftResponse
import com.user.ncard.vo.Shipping

/**
 * Created by trong-android-dev on 31/10/17.
 */
data class ShippingEvent(val shipping: Shipping, val chatMessage: ChatMessage) {
}