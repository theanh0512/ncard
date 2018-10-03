package com.user.ncard.ui.me

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.User
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class MyProfileViewModel @Inject constructor(val userRepository: UserRepository,
                                             val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val userData = MediatorLiveData<User>()

    fun getMe() {
        userRepository.getUserById(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), userData)
    }
}
