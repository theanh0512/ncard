package com.user.ncard.vo

/**
 * Created by dangui on 10/11/17.
 */
enum class ChatMessageContentType(val type: String) {
    UNKNOWN("unknown"),
    TEXT("text"),
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    LOCATION("location"),
    CREDIT("credit"),
    GIFT("gift"),
    UXCSYSTEM("uxcsystem")
}