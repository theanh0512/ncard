package com.user.ncard.ui.discovery

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.user.ncard.R
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.FriendRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Resource
import com.user.ncard.vo.User
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class FriendRequestViewModel @Inject constructor(val userRepository: UserRepository,
                                                 val friendRepository: FriendRepository,
                                                 val context: Context,
                                                 val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val start = MutableLiveData<Boolean>()
    val userList: LiveData<List<User>>
    val sentList: LiveData<List<User>>
    val acceptSuccess = SingleLiveEvent<Void>()
    val rejectSuccess = SingleLiveEvent<Void>()

    init {
        userList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<User>>()
            } else {
                return@switchMap userRepository.getAllFriendRequests()
            }
        }
        sentList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<User>>()
            } else {
                return@switchMap userRepository.getAllFriendRequestsSent()
            }
        }
    }

    fun acceptFriend(initiator: User) {
        userRepository.updateFriendRequest(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID),
                initiator, acceptSuccess, context.getString(R.string.friend_accept))
    }

    fun rejectFriend(initiator: User) {
        userRepository.updateFriendRequest(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID),
                initiator, rejectSuccess, context.getString(R.string.friend_reject))
    }

    fun regetALlFriends() {
        //startGetAllFriend.value = true
        friendRepository.regetALlFriends()
    }
}