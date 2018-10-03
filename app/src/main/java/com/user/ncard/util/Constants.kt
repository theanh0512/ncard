package com.user.ncard.util

import com.user.ncard.BuildConfig

/**
 * Created by Pham on 17/9/2017.
 */
class Constants {
    companion object {
        const val DEV = true
        const val BIGGEST_INT = 2147483647
        var BASE_URL = if(BuildConfig.DEBUG && DEV) "https://09ay5evpg2.execute-api.ap-northeast-1.amazonaws.com/dev/" else "https://o0ao5qx1b0.execute-api.ap-northeast-1.amazonaws.com/production/"
        var USER_INFO_SERVICE_URL = BASE_URL
        var FRIEND_REQUEST_SERVICE_URL = BASE_URL
        var FRIEND_INFO_SERVICE_URL = BASE_URL
        const val SCAN_NAME_CARD_URL = "http://bcr1.intsig.net/"
        //chat config
        val APP_ID = if(BuildConfig.DEBUG && DEV) "56229" else "62099"
        val AUTH_KEY = if(BuildConfig.DEBUG && DEV) "ZfvJhSaHgT-vCkd" else "fH-a-aGTMjUy9Vq"
        val AUTH_SECRET = if(BuildConfig.DEBUG && DEV) "ubaQgMC2TZdf2yc" else "V9Wqw-fjHp9ubND"
        val ACCOUNT_KEY = if(BuildConfig.DEBUG && DEV) "Fcm7YN4Har649VxUSd8z"  else "H-3XtvhJd2-dKL_qa5Gx"
        const val USERS_TAG = "webrtcusers"
        const val USERS_PASSWORD = "x6Bt0VDy5"
        const val PORT = 5223
        const val SOCKET_TIMEOUT = 180
        const val KEEP_ALIVE = true
        const val USE_TLS = true
        const val AUTO_JOIN = true
        const val AUTO_MARK_DELIVERED = false
        const val RECONNECTION_ALLOWED = true
        const val ALLOW_LISTEN_NETWORK = true
        const val CATEGORY_ECOMMERCE = "ecommerce"
        const val CATEGORY_WALLET = "wallet"
        //chat message custom params
        const val CHAT_CONTENT_TYPE = "chat_content_type"
        const val CHAT_FILE = "chat_file"
        const val CHAT_LOCATION = "chat_location"
        const val CHAT_CREDIT_TRANSACTION = "credit_transaction"
        const val CHAT_SYSTEM_INFO = "uxc_system_info"
        const val CHAT_GIFT = "gift"
        const val SYSTEM_MESSAGE_TYPE = "notification_type"
        const val SAVE_TO_HISTORY = "save_to_history"
        const val SENDER_DATE_SENT = "sender_date_sent"

        const val PROPERTY_DATE_SENT = "date_sent"
        const val PROPERTY_OCCUPANTS_IDS = "occupants_ids"
        const val PROPERTY_DIALOG_TYPE = "dialog_type"
        const val PROPERTY_DIALOG_NAME = "room_name"
        const val PROPERTY_DIALOG_ID = "_id"
        const val PROPERTY_ACTION_MAKERNAME = "actionMakerName"
        const val PROPERTY_ROOM_PHOTO = "room_photo"
    }
}