package com.user.ncard.ui.chats

import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.ui.chats.utils.gcm.NotificationUtils

/**
 * Created by trong-android-dev on 12/12/17.
 */
class GcmChatPushListner : CoreGcmPushListenerService() {
    private val NOTIFICATION_ID = 1

    override fun showNotification(message: String?) {
        NotificationUtils.showNotification(this, MainActivity::class.java,
                this.getString(R.string.app_name), message,
                R.mipmap.ic_launcher, NOTIFICATION_ID)
    }
}