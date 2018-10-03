package com.user.ncard.ui.card.profile

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.FriendRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.BaseEntity
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class SetRemarkViewModel @Inject constructor(val friendRepository: FriendRepository,
                                             val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val successEvent = SingleLiveEvent<Void>()
    val remark: ObservableField<String> = ObservableField()

    fun updateRemark(friend: BaseEntity) {
        friendRepository.updateFriendRemark(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), friend, successEvent, if(remark?.get() == null) "" else remark.get()!!)
    }
}