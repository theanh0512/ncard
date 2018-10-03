package com.user.ncard.util

import android.util.Log
import com.quickblox.core.exception.QBResponseException
import android.os.Bundle
import android.text.TextUtils
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.users.model.QBUser
import com.quickblox.core.QBEntityCallback
import com.quickblox.users.QBUsers
import com.user.ncard.vo.User
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.chat.request.QBDialogRequestBuilder
import com.quickblox.chat.utils.DialogUtils
import com.quickblox.core.LogLevel
import com.quickblox.core.request.QBPagedRequestBuilder
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.core.request.QBRequestUpdateBuilder
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.chats.quickblox.QbEntityCallbackTwoTypeWrapper
import com.user.ncard.ui.chats.quickblox.QbEntityCallbackWrapper
import com.user.ncard.ui.chats.quickblox.QbUsersHolder
import com.user.ncard.vo.Friend
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smackx.muc.DiscussionHistory
import java.util.ArrayList


/**
 * Created by dangui on 30/10/17.
 */
public class ChatHelper private constructor() {
    var TAG: String = "ChatHelper"
    var currentUser: QBUser? = null
    var qbChatService: QBChatService

    companion object {
        val instance: ChatHelper by lazy { Holder.INSTANCE }
    }

    private object Holder {
        val INSTANCE = ChatHelper()
    }

    init {
        Log.e(TAG, "ChatHelper init.");
        /*qbChatService = QBChatService.getInstance()
        qbChatService.setUseStreamManagement(true)*/
        //QBSettings.getInstance().logLevel = LogLevel.DEBUG
        //QBChatService.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(buildChatConfigs())
        qbChatService = QBChatService.getInstance()
        //qbChatService.setUseStreamManagement(true)

    }

    fun getCurrentChatService(): QBChatService {
        if (qbChatService == null) {
            //QBSettings.getInstance().logLevel = LogLevel.DEBUG
            //QBChatService.setDebugEnabled(true)
            QBChatService.setDefaultPacketReplyTimeout(150000)
            QBChatService.setDefaultConnectionTimeout(150000)
            QBChatService.setConfigurationBuilder(buildChatConfigs())
            qbChatService = QBChatService.getInstance()

        }
        return qbChatService
    }

    private fun buildChatConfigs(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()
        configurationBuilder.port = Constants.PORT
        configurationBuilder.socketTimeout = Constants.SOCKET_TIMEOUT
        configurationBuilder.isUseTls = Constants.USE_TLS
        configurationBuilder.isKeepAlive = Constants.KEEP_ALIVE
        configurationBuilder.isAutojoinEnabled = Constants.AUTO_JOIN

        configurationBuilder.setAutoMarkDelivered(Constants.AUTO_MARK_DELIVERED)
        configurationBuilder.isReconnectionAllowed = Constants.RECONNECTION_ALLOWED
        configurationBuilder.setAllowListenNetwork(Constants.ALLOW_LISTEN_NETWORK)

        return configurationBuilder
    }

    fun getCurrentQBUser(): QBUser {
        return QBChatService.getInstance().user
    }

    fun signUpAndSignIn(user: User, sharedPreferenceHelper: SharedPreferenceHelper,
                        signUpListener: SignUpListener?,
                        signInListener: SignInListener?) {
        //check if qb user exists
        Utils.Log(TAG, "-> QBUsers.getUserByEmail")
        QBUsers.getUserByEmail(user.email).performAsync(object : QBEntityCallback<QBUser> {

            override fun onSuccess(qbUser: QBUser, args: Bundle) {
                Utils.Log(TAG, "<- QBUsers.getUserByEmail onSuccess: " + qbUser.toString())
                signUpListener?.onAlreadySignedUp(qbUser)
                signIn(user, sharedPreferenceHelper, signInListener)
            }

            override fun onError(errors: QBResponseException) {
                Utils.Log(TAG, "<- QBUsers.getUserByEmail onError: " + errors.message)
                signUp(user, sharedPreferenceHelper, signUpListener, signInListener)
            }
        })
    }

    private fun signUp(user: User, sharedPreferenceHelper: SharedPreferenceHelper,
                       signUpListener: SignUpListener?,
                       signInListener: SignInListener?) {
        val chatLoginUsername = user.username
        val chatPassword = user.chatPassword
        val myQbUser = QBUser(chatLoginUsername, chatPassword)
        myQbUser.email = user.email
        myQbUser.externalId = user.id.toString()
        if (!TextUtils.isEmpty(user.firstName) && !TextUtils.isEmpty(user.lastName)) {
            myQbUser.fullName = user.firstName + user.lastName
        }

        Utils.Log(TAG, "-> QBUsers.signUp")
        QBUsers.signUp(myQbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, args: Bundle) {
                // success
                Utils.Log(TAG, "<- QBUsers.signUp onSuccess: " + qbUser.toString())
                signIn(user, sharedPreferenceHelper, signInListener)

                signUpListener?.onSignUpSuccess(qbUser)
            }

            override fun onError(error: QBResponseException) {
                // error
                Utils.Log(TAG, "<- QBUsers.signUp onError:" + error.message)

                signUpListener?.onSignUpError()
            }
        })
    }

    fun signIn(user: User, sharedPreferenceHelper: SharedPreferenceHelper,
               signInListener: SignInListener?) {
        val email = user.email;
        val password = user.chatPassword;

        Utils.Log(TAG, "-> QBUsers.signInByEmail")
        QBUsers.signInByEmail(email, password).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser, args: Bundle) {
                // success
                Utils.Log(TAG, "<- QBUsers.signInByEmail onSuccess: " + qbUser.toString())

                currentUser = qbUser

                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CHAT_USER_ID, qbUser.id)
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CHAT_LOGIN_USERNAME, qbUser.login)
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.CHAT_PASSWORD, password)
                sharedPreferenceHelper.put(SharedPreferenceHelper.Key.IS_SIGN_UP_SIGN_IN_CHAT_SUCCESS, true)

                signInListener?.onSignInSuccess(qbUser)
            }

            override fun onError(error: QBResponseException) {
                // error
                Utils.Log(TAG, "<- QBUsers.signInByEmail onError: " + error.message)

                signInListener?.onSignInError()
            }
        })
    }

    fun signOut() {
        //disconnect the chat connection
        Utils.Log(TAG, "disconnect the chat connection")
        QBChatService.getInstance().logout()
        //sign out chat account
        Utils.Log(TAG, "sign out chat account")
        QBUsers.signOut()
    }

    fun fetchQbUser(email: String, listener: FetchQbUserListener?) {
        QBUsers.getUserByEmail(email).performAsync(object : QBEntityCallback<QBUser> {

            override fun onSuccess(qbUser: QBUser, args: Bundle) {
                Utils.Log(TAG, "<- QBUsers.getUserByEmail onSuccess: " + qbUser.toString())
                listener?.onFetchSuccess(qbUser)
            }

            override fun onError(errors: QBResponseException) {
                Utils.Log(TAG, "<- QBUsers.getUserByEmail onError: " + errors.message)
                listener?.onFetchError()
            }
        })
    }

    fun connect(sharedPreferenceHelper: SharedPreferenceHelper, listener: ChatConnectListener) {
        val chatService = QBChatService.getInstance()
        if (chatService.isLoggedIn) {
            Utils.Log(TAG, "already logged in")
            listener?.onConnectLoginSuccess()
            return
        }
        val chatUserId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CHAT_USER_ID, -1)
        val chatLoginUsername = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CHAT_LOGIN_USERNAME, "no login username")
        val chatPassword = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CHAT_PASSWORD, "no_chat_password")
        val myQbUser = QBUser(chatLoginUsername, chatPassword)
        myQbUser.id = chatUserId
        Utils.Log(TAG, "-> ChatService.login")
        chatService.login(myQbUser, object : QBEntityCallback<QBUser> {
            override fun onSuccess(qbUser: QBUser?, args: Bundle) {
                Utils.Log(TAG, "<- ChatService.login onSuccess")
                listener?.onConnectLoginSuccess()
            }

            override fun onError(errors: QBResponseException?) {
                Utils.Log(TAG, "<- ChatService.login onError: " + errors?.message)
                listener.onConnectLoginError()
            }
        })
    }

    /* Create Chat Dialog */
    fun createPrivateChatDialog(recipientId: Int?, listener: CreateChatDialogListener?) {
        if (recipientId == null) {
            Utils.Log(TAG, "createPrivateChatDialog recipient Id null")
            return
        }
        val dialog = DialogUtils.buildPrivateDialog(recipientId);
        Utils.Log(TAG, "-> createPrivateChatDialog - recipientId:" + recipientId)
        QBRestChatService.createChatDialog(dialog).performAsync(object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(result: QBChatDialog, params: Bundle) {
                Utils.Log(TAG, "<- createPrivateChatDialog onSuccess")
                listener?.onCreateSuccess(result)
            }

            override fun onError(responseException: QBResponseException?) {
                Utils.Log(TAG, "<- createPrivateChatDialog onError: " + responseException?.message)
                listener?.onCreateError()
            }
        })
    }

    fun createChatDialog(occupantIds: List<Int>, name: String?, listener: CreateChatDialogListener?) {
        var dialog: QBChatDialog
        if (occupantIds.size == 1) {
            dialog = DialogUtils.buildPrivateDialog(occupantIds[0]);
        } else {
            dialog = DialogUtils.buildDialog(name, QBDialogType.GROUP, occupantIds)
        }
        QBRestChatService.createChatDialog(dialog).performAsync(object : QBEntityCallback<QBChatDialog> {
            override fun onSuccess(result: QBChatDialog, params: Bundle) {
                Utils.Log(TAG, "<- createChatDialog onSuccess " + result.type.code)
                listener?.onCreateSuccess(result)
            }

            override fun onError(responseException: QBResponseException?) {
                Utils.Log(TAG, "<- createChatDialog onError: " + responseException?.message)
                listener?.onCreateError()
            }
        })
    }
    /* Create Chat Dialog */

    /* Dialog */
    val DIALOG_ITEMS_PER_PAGE = 100

    fun getDialogs(customObjectRequestBuilder: QBRequestGetBuilder, callback: QBEntityCallback<ArrayList<QBChatDialog>>) {
        customObjectRequestBuilder.limit = DIALOG_ITEMS_PER_PAGE

        QBRestChatService.getChatDialogs(null, customObjectRequestBuilder).performAsync(
                object : QbEntityCallbackWrapper<ArrayList<QBChatDialog>>(callback) {
                    override fun onSuccess(dialogs: ArrayList<QBChatDialog>, args: Bundle) {
                        val dialogIterator = dialogs.iterator()
                        while (dialogIterator.hasNext()) {
                            val dialog = dialogIterator.next()
                            if (dialog.type == QBDialogType.PUBLIC_GROUP) {
                                dialogIterator.remove()
                            }
                        }
                        callback.onSuccess(dialogs, args)
                        // getUsersFromDialogs(dialogs, callback)
                        // Not calling super.onSuccess() because
                        // we want to load chat users before triggering callback
                    }
                })
    }

    fun getDialogById(dialogId: String, callback: QBEntityCallback<QBChatDialog>) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(callback)
    }

    private fun getUsersFromDialogs(dialogs: ArrayList<QBChatDialog>,
                                    callback: QBEntityCallback<ArrayList<QBChatDialog>>) {
        val userIds = ArrayList<Int>()
        for (dialog in dialogs) {
            userIds.addAll(dialog.occupants)
            userIds.add(dialog.lastMessageUserId)
        }

        val requestBuilder = QBPagedRequestBuilder(userIds.size, 1)
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                object : QbEntityCallbackTwoTypeWrapper<ArrayList<QBUser>, ArrayList<QBChatDialog>>(callback) {
                    override fun onSuccess(users: ArrayList<QBUser>, params: Bundle) {
                        QbUsersHolder.getInstance().putUsers(users)
                        callback.onSuccess(dialogs, params)
                    }
                })
    }

    fun getUsersFromDialog(dialog: QBChatDialog,
                           callback: QBEntityCallback<ArrayList<QBUser>>) {
        val userIds = dialog.occupants

        val users = ArrayList<QBUser>(userIds.size)
        for (id in userIds) {
            users.add(QbUsersHolder.getInstance().getUserById(id))
        }

        // If we already have all users in memory
        // there is no need to make REST requests to QB
        if (userIds.size == users.size) {
            callback.onSuccess(users, null)
            return
        }

        val requestBuilder = QBPagedRequestBuilder(userIds.size, 1)
        QBUsers.getUsersByIDs(userIds, requestBuilder).performAsync(
                object : QbEntityCallbackWrapper<ArrayList<QBUser>>(callback) {
                    override fun onSuccess(qbUsers: ArrayList<QBUser>, bundle: Bundle) {
                        QbUsersHolder.getInstance().putUsers(qbUsers)
                        callback.onSuccess(qbUsers, bundle)
                    }
                })
    }
    /* Dialog */

    /* Get Chat Messages from Dialog */
    val CHAT_HISTORY_ITEMS_PER_PAGE = 50
    val CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent"
    val skipPagination = 0;
    fun getChatMessagesFromDialog(dialog: QBChatDialog,
                                  callback: QBEntityCallback<ArrayList<QBChatMessage>>) {
        val customObjectRequestBuilder = QBRequestGetBuilder()
        customObjectRequestBuilder.skip = skipPagination
        customObjectRequestBuilder.limit = CHAT_HISTORY_ITEMS_PER_PAGE
        customObjectRequestBuilder.sortDesc(CHAT_HISTORY_ITEMS_SORT_FIELD)

        QBRestChatService.getDialogMessages(dialog, customObjectRequestBuilder).performAsync(
                object : QbEntityCallbackWrapper<ArrayList<QBChatMessage>>(callback) {
                    override fun onSuccess(qbChatMessages: ArrayList<QBChatMessage>, bundle: Bundle) {

                        callback.onSuccess(qbChatMessages, bundle)
                    }
                })
    }

    fun sendChatMessage(dialog: QBChatDialog, qbChatMessage: QBChatMessage,
                        callback: QBEntityCallback<Void>) {
        Functions.showLogMessage("Trong", "real sendChatMessage  " + dialog.name + " with " + qbChatMessage.id)
        dialog.sendMessage(qbChatMessage, object : QbEntityCallbackWrapper<Void>(callback) {
            override fun onError(error: QBResponseException?) {
                callback.onError(error)
            }

            override fun onSuccess(t: Void?, bundle: Bundle?) {
                callback.onSuccess(t, bundle)
            }
        })
    }

    fun sendChatMessageWithoutJoined(dialog: QBChatDialog, qbChatMessage: QBChatMessage,
                        callback: QBEntityCallback<Void>) {
        dialog.sendMessageWithoutJoin(qbChatMessage, object : QbEntityCallbackWrapper<Void>(callback) {
            override fun onError(error: QBResponseException?) {
                callback.onError(error)
            }

            override fun onSuccess(t: Void?, bundle: Bundle?) {
                callback.onSuccess(t, bundle)
            }
        })
    }

    fun deleteChatMessage(dialog: QBChatDialog, messageId: String,
                          callback: QBEntityCallback<Void>) {
        QBRestChatService.deleteMessage(messageId, false).performAsync(object : QbEntityCallbackWrapper<Void>(callback) {
            override fun onError(error: QBResponseException?) {
                callback.onError(error)
            }

            override fun onSuccess(t: Void?, bundle: Bundle?) {
                callback.onSuccess(t, bundle)
            }
        })
    }

    fun exitFromDialog(qbChatDialog: QBChatDialog, callback: QBEntityCallback<QBChatDialog>) {
        try {
            leaveChatDialog(qbChatDialog)
        } catch (e: XMPPException) {
            callback.onError(QBResponseException(e.message))
        } catch (e: SmackException.NotConnectedException) {
            callback.onError(QBResponseException(e.message))
        }

        val qbRequestBuilder = QBDialogRequestBuilder()
        qbRequestBuilder.removeUsers(QBChatService.getInstance().user.id!!)

        QBRestChatService.updateGroupChatDialog(qbChatDialog, qbRequestBuilder).performAsync(callback)
    }

    fun updateDialogUsers(name: String,
                          qbChatDialog: QBChatDialog,
                          friendsList: List<Friend>,
                          callback: QBEntityCallback<QBChatDialog>) {


        val friendIds = friendsList.map { it.chatId!! }

        var friendIdsRemoved: MutableList<Int> = ArrayList<Int>()
        qbChatDialog?.occupants?.forEach {
            if (!findIdInListIds(it, friendIds)) {
                friendIdsRemoved.add(it)
            }
        }

        var friendIdsAdded: MutableList<Int> = ArrayList<Int>()
        friendIds?.forEach {
            if (!findIdInListIds(it, qbChatDialog?.occupants)) {
                friendIdsAdded.add(it)
            }
        }

        val qbRequestBuilder = QBDialogRequestBuilder()
        if (!friendIdsRemoved.isEmpty()) {
            qbRequestBuilder.removeUsers(*friendIdsRemoved.toIntArray())
        }
        if (!friendIdsAdded.isEmpty()) {
            qbRequestBuilder.addUsers(*friendIdsAdded.toIntArray())
        }
        qbChatDialog.name = name

        QBRestChatService.updateGroupChatDialog(qbChatDialog, qbRequestBuilder).performAsync(callback)
    }

    fun findIdInListIds(id: Int, friendIds: List<Int>?): Boolean {
        friendIds?.forEach {
            if (it == id) {
                return true
            }
        }
        return false
    }

    fun join(chatDialog: QBChatDialog, callback: QBEntityCallback<Void>) {
        val history = DiscussionHistory()
        history.maxStanzas = 0

        chatDialog.join(history, callback)
    }

    @Throws(XMPPException::class, SmackException.NotConnectedException::class)
    fun leaveChatDialog(chatDialog: QBChatDialog) {
        chatDialog.leave()
    }

    interface SignUpListener {
        fun onAlreadySignedUp(qbUser: QBUser)
        fun onSignUpSuccess(qbUser: QBUser)
        fun onSignUpError()
    }

    interface SignInListener {
        fun onSignInSuccess(qbUser: QBUser)
        fun onSignInError()
    }

    interface FetchQbUserListener {
        fun onFetchSuccess(qbUser: QBUser)
        fun onFetchError()
    }

    interface CreateChatDialogListener {
        fun onCreateSuccess(qbChatDialog: QBChatDialog)
        fun onCreateError()
    }

    open interface ChatConnectListener {
        fun onConnectLoginSuccess()
        fun onConnectLoginError()
    }
}