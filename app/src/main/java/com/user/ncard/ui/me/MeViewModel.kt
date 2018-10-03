package com.user.ncard.ui.me

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.user.ncard.repository.*
import com.user.ncard.util.AppHelper
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.NCardInfo
import com.user.ncard.vo.User
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class MeViewModel @Inject constructor(val userRepository: UserRepository,
                                      val friendRepository: FriendRepository,
                                      val nameCardRepository: NameCardRepository,
                                      val chatRepository: ChatRepository,
                                      val catalogueRepository: CatalogueRepository,
                                      context: Context,
                                      val sharedPreferenceHelper: SharedPreferenceHelper,
                                      val chatHelper: ChatHelper) : ViewModel() {

    val userData = MediatorLiveData<User>()

    fun getMe() {
        userRepository.getUserById(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), userData)
    }

    fun logout() {
        // Sign out Cognito
        AppHelper.getPool().getUser(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)).signOut()
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.IS_JUST_LOGIN, true)

        //logout chat
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.IS_SIGN_UP_SIGN_IN_CHAT_SUCCESS, false)
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.IS_UPDATE_CHAT_ID_SUCCESS, false)
        //sharedPreferenceHelper.remove(SharedPreferenceHelper.Key.CURRENT_USERNAME)
        sharedPreferenceHelper.remove(SharedPreferenceHelper.Key.CURRENT_USER_ID)
        sharedPreferenceHelper.remove(SharedPreferenceHelper.Key.CHAT_USER_ID)
        sharedPreferenceHelper.remove(SharedPreferenceHelper.Key.CHAT_LOGIN_USERNAME)
        sharedPreferenceHelper.remove(SharedPreferenceHelper.Key.CHAT_PASSWORD)
        sharedPreferenceHelper.remove(SharedPreferenceHelper.Key.CURRENT_USER_FIRST_NAME)
        sharedPreferenceHelper.remove(SharedPreferenceHelper.Key.CURRENT_USER_LAST_NAME)
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.FIRST_TIME_LOAD_DIALOG_LIST, false)
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.SIGNOUT_REMAIN_DB, false)

        // Sign out chat
        chatHelper.signOut()
        // TODO: need to remove db
        chatRepository.deleteChatDB()
        catalogueRepository.deleteCataloguePostDB()
        userRepository.deleteUserDB()
        friendRepository.deleteFriendDB()
        nameCardRepository.deleteNameCardDB()
    }

    fun logoutStillRemainDb() {
        // Sign out Cognito
        AppHelper.getPool().getUser(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)).signOut()
        sharedPreferenceHelper.put(SharedPreferenceHelper.Key.SIGNOUT_REMAIN_DB, true)
    }
}