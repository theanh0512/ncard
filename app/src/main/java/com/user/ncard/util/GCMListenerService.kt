package com.user.ncard.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.google.android.gms.gcm.GcmListenerService
import com.user.ncard.MainActivity
import com.user.ncard.NCardApplication
import com.user.ncard.R
import com.user.ncard.di.DaggerAppComponent
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.FriendRepository
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.chats.GroupDialogUpdateEvent
import com.user.ncard.ui.chats.utils.DialogManager
import com.user.ncard.ui.discovery.RequestRecommendationUpdateEvent
import com.user.ncard.vo.Friend
import com.user.ncard.vo.GcmNotificationType
import com.user.ncard.vo.Resource
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


/**
 * Created by changmingniu on 3/5/16.
 */
class GCMListenerService : GcmListenerService() {
    // MARK - Push notifications
    companion object {
        const val nc_create_friend_request = "You have a friend request.";
        const val nc_accept_friend_request = "%s has accepted your friend request.";
        const val nc_share_friend = "%s has shared a friend contact with you";
        const val nc_share_card = "%s has shared a name card with you";
        const val nc_wallet_transfered_expired = "Money Transfer has been expired";
        const val nc_wallet_transfer_expired = "Money Transfer has been expired";
        const val nc_gift_transfered_expired = "Gift has been expired";
        const val nc_gift_transfer_expired = "Gift has been expired";
        const val nc_withdraw_accepted = "Your money withdrawal is accepted";
        const val nc_withdraw_rejected = "Your money withdrawal is rejected";
    }

    override fun onMessageReceived(from: String?, data: Bundle?) {
        Log.e("NCard", "from " + from)
        Log.e("NCard", "Receive pushed notification " + data!!.toString())
        sendNotification(data)
    }

    private fun sendNotification(data: Bundle) {

        val stackBuilder = TaskStackBuilder.create(this)
        var intent: Intent? = null
        var title = getString(R.string.application_name)
        val action = data.getString("action")
        val dialog_id = data.getString("dialog_id")
        var message = data.getString("message")
        var senderName = data.getString("senderName")
        if (action != null) {
            when (action) {
                GcmNotificationType.NC_CREATE_FRIEND_REQUEST.type -> { title = getString(R.string.friend_request); message = nc_create_friend_request; broadcastToMainActivity(); regetFriendRequest() }
                GcmNotificationType.NC_ACCEPT_FRIEND_REQUEST.type -> { title = getString(R.string.friend_request); message = if(senderName != null) nc_accept_friend_request.format(senderName) else message; regetALlFriends(); }
                GcmNotificationType.NC_SHARE_FRIEND.type -> { title = getString(R.string.friend_recommendation); message = if(senderName != null) nc_share_friend.format(senderName) else message ; regetFriendRecommendation()}
                GcmNotificationType.NC_SHARE_CARD.type -> { title = getString(R.string.title_share_card);message = if(senderName != null) nc_share_card.format(senderName) else message; regetCardRecommendation() }
                GcmNotificationType.NC_WALLET_TRANSFERED_EXPIRED.type -> { title = getString(R.string.title_credit_transfer); message = nc_wallet_transfered_expired}
                GcmNotificationType.NC_WALLET_TRANSFER_EXPIRED.type -> { title = getString(R.string.title_credit_transfer); message = nc_wallet_transfer_expired}
                GcmNotificationType.NC_GIFT_TRANSFERED_EXPIRED.type -> { title = getString(R.string.title_gift_send); message = nc_gift_transfered_expired}
                GcmNotificationType.NC_GIFT_TRANSFER_EXPIRED.type -> { title = getString(R.string.title_gift_send); message = nc_gift_transfer_expired}
                GcmNotificationType.NC_WITHDRAW_ACCEPTED.type -> { title = getString(R.string.title_withdraw); message = nc_withdraw_accepted}
                GcmNotificationType.NC_WITHDRAW_REJECTED.type -> { title = getString(R.string.title_withdraw); message = nc_withdraw_rejected}
                GcmNotificationType.NC_DELETE_FRIEND.type -> { regetALlFriends() }
            }
        }
        if (dialog_id != null) {
            processChatNotification(dialog_id)
            title =  getString(R.string.title_chat)
        }
        intent = Intent(this, MainActivity::class.java)
        stackBuilder.addNextIntent(intent)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        if (action != GcmNotificationType.NC_DELETE_FRIEND.type) {
            buildNotification(title, message, stackBuilder, false, false)
        }
    }

    private fun broadcastToMainActivity() {
        val intent = Intent(MainActivity.FRIEND_REQUEST)
        applicationContext.sendBroadcast(intent)
    }

    private fun buildNotification(title: String, message: String?, stackBuilder: TaskStackBuilder, enableSound: Boolean, enableVibrate: Boolean) {
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            R.drawable.ic_launcher
                        else
                            R.drawable.ic_launcher)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        if (enableSound) {
            notificationBuilder.setSound(defaultSoundUri)
        }

        if (enableVibrate) {
            notificationBuilder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /*Friend*/
    @Inject lateinit var friendRepository: FriendRepository

    fun regetALlFriends() {
        friendRepository.regetALlFriends()
    }

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var nameCardRepository: NameCardRepository

    // TODO: Live data friend request list not updated, don't know why,  work around here: update manually after finish request
    fun regetFriendRequest() {
        Log.e("NCard", "regetFriendRequest")
        userRepository.getAllFriendRequests()
        val handler = Handler(Looper.getMainLooper())
        handler.post({
            Handler().postDelayed({
                EventBus.getDefault().post(RequestRecommendationUpdateEvent(""))
            }, 1500)
        })
    }

    // TODO: Live data friend request list not updated, don't know why,  work around here: update manually after finish request
    fun regetFriendRecommendation() {
        Log.e("NCard", "regetFriendRecommendation")
        userRepository.getAllFriendShared()
        val handler = Handler(Looper.getMainLooper())
        handler.post({
            Handler().postDelayed({
                EventBus.getDefault().post(RequestRecommendationUpdateEvent(""))
            }, 1500)
        })
    }

    // TODO: Live data friend request list not updated, don't know why,  work around here: update manually after finish request
    fun regetCardRecommendation() {
        Log.e("NCard", "regetCardRecommendation")
        nameCardRepository.getAllNameCardShared()
        val handler = Handler(Looper.getMainLooper())
        handler.post({
            Handler().postDelayed({
                EventBus.getDefault().post(RequestRecommendationUpdateEvent(""))
            }, 1500)
        })
    }

    /*Friend*/

    /* Chat */
    override fun onCreate() {
        super.onCreate()
        // TODO: need inject manually here, improve later(need change structure of AppInjector)
        DaggerAppComponent.builder().application((application as NCardApplication))
                .build().inject(this)
    }

    @Inject lateinit var chatHelper: ChatHelper
    @Inject lateinit var chatRepository: ChatRepository
    @Inject lateinit var sharedPreferenceHelper: SharedPreferenceHelper
    var dialogManager: DialogManager? = null

    fun getDialogManagerInstance(): DialogManager? {
        if (dialogManager == null) {
            dialogManager = DialogManager(this, chatHelper, chatRepository, sharedPreferenceHelper)
        }
        return dialogManager
    }

    fun processChatNotification(dialogId: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.post({
            getDialogManagerInstance()?.updateDialog(dialogId)
        })
    }
    /* Chat */
}