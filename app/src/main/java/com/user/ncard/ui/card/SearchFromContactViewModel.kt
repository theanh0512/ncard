package com.user.ncard.ui.card

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Resource
import com.user.ncard.vo.User
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class SearchFromContactViewModel @Inject constructor(val userRepository: UserRepository,
                                                     val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val start = MutableLiveData<Boolean>()
    val userList: LiveData<Resource<List<User>>>
    val sendRequestSuccess = SingleLiveEvent<Void>()

    init {
        userList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<User>>>()
            } else {
                return@switchMap userRepository.getUserFromContact()
            }
        }
    }

    fun createFriendRequest(user: User) {
        userRepository.createFriendRequest(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), user, sendRequestSuccess)
    }
}