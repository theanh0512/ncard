package com.user.ncard.ui.card.namecard

import android.arch.lifecycle.ViewModel
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.NameCard
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class NameCardMoreViewModel @Inject constructor(val nameCardRepository: NameCardRepository,
                                                val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val successEvent = SingleLiveEvent<Void>()

    fun deleteNameCard(nameCard: NameCard, isMyNameCard:Boolean) {
        nameCardRepository.deleteNameCard(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), nameCard, successEvent,isMyNameCard)
    }
}