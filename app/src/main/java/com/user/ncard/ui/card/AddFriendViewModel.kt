package com.user.ncard.ui.card

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.UserRepository
import com.user.ncard.vo.User
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class AddFriendViewModel @Inject constructor(val userRepository: UserRepository) : ViewModel() {
    val userData = MutableLiveData<User>()
    val getUserSuccessEvent = SingleLiveEvent<Void>()
    val getUserFailureEvent = SingleLiveEvent<Void>()

    fun getUser(userName: String) {
        userRepository.getUserFromQRCode(userName, userData, getUserSuccessEvent, getUserFailureEvent)
    }
}