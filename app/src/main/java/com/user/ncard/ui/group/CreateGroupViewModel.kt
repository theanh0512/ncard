package com.user.ncard.ui.group

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.FriendRepository
import com.user.ncard.repository.GroupRepository
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Friend
import com.user.ncard.vo.Group
import com.user.ncard.vo.NameCard
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class CreateGroupViewModel @Inject constructor(val groupRepository: GroupRepository,
                                               val nameCardRepository: NameCardRepository,
                                               val friendRepository: FriendRepository,
                                               val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val createGroupSuccess = SingleLiveEvent<Void>()
    val groupLiveData = MutableLiveData<Group>()
    val friendList: MediatorLiveData<List<Friend>> = MediatorLiveData()
    val nameCardList: MediatorLiveData<List<NameCard>> = MediatorLiveData()
    val deleteGroupSuccess = SingleLiveEvent<Void>()

    fun createGroup(group: Group) {
        groupRepository.createGroup(group, createGroupSuccess, groupLiveData)
    }

    fun updateGroup(group: Group) {
        groupRepository.updateGroup(group, createGroupSuccess)
    }

    fun getNameCardAndFriendForGroup(group: Group) {
        if (group.nameCards?.isNotEmpty() == true)
        {
            val nameCards = ArrayList<Int>()
            group.nameCards?.forEach{
                if(it!=null) nameCards.add(it.id)
            }
            nameCardRepository.getNameCardsByIdFromDb(nameCards,nameCardList)
        }
        if (group.members?.isNotEmpty() == true){
            val members = ArrayList<Int>()
            group.members?.forEach{
                if(it!=null) members.add(it.id)
            }
            friendRepository.getFriendsByIdFromDb(members,friendList)
        }
    }

    fun deleteGroup(groupId: Int) {
        groupRepository.deleteGroup(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), groupId, deleteGroupSuccess)
    }
}