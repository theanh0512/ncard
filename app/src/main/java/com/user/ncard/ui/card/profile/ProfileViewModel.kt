package com.user.ncard.ui.card.profile

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.JobRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Job
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.User
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class ProfileViewModel @Inject constructor(val userRepository: UserRepository,
                                           val sharedPreferenceHelper: SharedPreferenceHelper,
                                           val chatRepository: ChatRepository, val jobRepository: JobRepository) : ViewModel() {
    val getUserJob = MutableLiveData<Boolean>()
    val getCards = MutableLiveData<Boolean>()
    val getMutualFriend = MutableLiveData<Boolean>()
    val jobList: LiveData<List<Job>>
    val cardList: LiveData<List<NameCard>>
    val mutualFriendList: LiveData<List<Friend>>
    var userId = 0

    init {
        jobList = Transformations.switchMap(getUserJob) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<Job>>()
            } else {
                return@switchMap jobRepository.getJobsForUser(userId)
            }
        }
        cardList = Transformations.switchMap(getCards) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<NameCard>>()
            } else {
                return@switchMap jobRepository.getNameCardForUser(userId)
            }
        }
        mutualFriendList = Transformations.switchMap(getMutualFriend) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<Friend>>()
            } else {
                return@switchMap userRepository.getMutualFriend(userId)
            }
        }
    }

    val sendRequestSuccess = SingleLiveEvent<Void>()
    fun createFriendRequest(user: User) {
        userRepository.createFriendRequest(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), user, sendRequestSuccess)
    }

    val user = MutableLiveData<User>()
    fun getUserById(userId: Int) {
        userRepository.getUserById(userId, user)
    }

}