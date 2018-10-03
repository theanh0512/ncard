package com.user.ncard.ui.discovery

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.FriendRecommendation
import com.user.ncard.vo.NameCardRecommendation
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class FriendRecommendationViewModel @Inject constructor(val userRepository: UserRepository, val nameCardRepository: NameCardRepository,
                                                        val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val start = MutableLiveData<Boolean>()
    val userList: LiveData<List<FriendRecommendation>>
    val nameCardList: LiveData<List<NameCardRecommendation>>
    val successEvent = SingleLiveEvent<Void>()

    init {
        userList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<FriendRecommendation>>()
            } else {
                return@switchMap userRepository.getAllFriendShared()
            }
        }
        nameCardList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<NameCardRecommendation>>()
            } else {
                return@switchMap nameCardRepository.getAllNameCardShared()
            }
        }
    }

    fun updateFriendSharing(user: FriendRecommendation, status: String) {
        userRepository.updateFriendSharing(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), user, status, successEvent)
    }
    fun updateNameCardSharing(user: NameCardRecommendation, status: String) {
        nameCardRepository.updateNameCardSharing(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), user, status, successEvent)
    }
}