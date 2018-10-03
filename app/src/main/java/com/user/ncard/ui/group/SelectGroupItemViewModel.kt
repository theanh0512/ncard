package com.user.ncard.ui.group

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.FriendRepository
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.BaseEntity
import com.user.ncard.vo.Friend
import com.user.ncard.vo.NameCard
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class SelectGroupItemViewModel @Inject constructor(val friendRepository: FriendRepository,
                                                   val nameCardRepository: NameCardRepository,
                                                   val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val userList: MediatorLiveData<List<Friend>> = MediatorLiveData()
    val nameCardList: MediatorLiveData<List<NameCard>> = MediatorLiveData()
    val successShareEvent = SingleLiveEvent<Void>()

    fun getFriendsAndNameCard() {
        friendRepository.getFriendsFromDb(userList)
        nameCardRepository.getNameCardsFromDb(nameCardList)
    }

    fun shareFriend(friend: BaseEntity, to: ArrayList<Int>) {
        friendRepository.shareFriend(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), friend, to, successShareEvent)
    }

    fun shareNameCard(nameCard: NameCard, to: ArrayList<Int>) {
        nameCardRepository.shareNameCard(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), nameCard, to, successShareEvent)
    }
}