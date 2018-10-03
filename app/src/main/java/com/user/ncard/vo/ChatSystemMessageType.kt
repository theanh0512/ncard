package com.user.ncard.vo

/**
 * Created by dangui on 10/11/17.
 */
enum class ChatSystemMessageType(val type: String) {
    UNKNOWN("unknown"),
    GROUP_CREATE("groupCreate"),
    GROUP_UPDATE("groupUpdate"),
    CONTACT_REQUEST("contactRequest"),
    CONTACT_ACCEPT("contactAccept"),
    CONTACT_REJECT("contactReject"),
    CONTACT_DELETE("contactDelete"),
}