package com.user.ncard.ui.card

import android.arch.lifecycle.*
import com.user.ncard.repository.FriendRepository
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Friend
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.Resource
import com.user.ncard.vo.UserFilter
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class CardsViewModel @Inject constructor(val friendRepository: FriendRepository,
                                         val nameCardRepository: NameCardRepository) : ViewModel() {
    val start = MutableLiveData<Boolean>()
    val userList: LiveData<Resource<List<Friend>>>
    val nameCardList: LiveData<Resource<List<NameCard>>>
    val searchedUserList: MediatorLiveData<List<Friend>> = MediatorLiveData()
    val searchedNameCardList: MediatorLiveData<List<NameCard>> = MediatorLiveData()

    init {
        userList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<Friend>>>()
            } else {
                return@switchMap friendRepository.getALlFriends()
            }
        }
        nameCardList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<NameCard>>>()
            } else {
                return@switchMap nameCardRepository.getALlNameCards()
            }
        }
    }

    fun getFriendsAndNameCardWithFilter(userFilter: UserFilter) {
        friendRepository.getFriendsWithFilter(searchedUserList, userFilter)
        nameCardRepository.getNameCardsWithFilter(searchedNameCardList, userFilter)
    }

    fun getFriendsAndNameCardWithoutFilter(name: String) {
        friendRepository.getFriendsWithoutFilter(searchedUserList, name)
        nameCardRepository.getNameCardsWithoutFilter(searchedNameCardList, name)
    }
}