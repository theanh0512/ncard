package com.user.ncard.ui.card.namecard

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableParcelable
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.UserFilter
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class EditNameCardViewModel @Inject constructor(val nameCardRepository: NameCardRepository,
                                                val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val query = MutableLiveData<UserFilter>()
    val nameCard = ObservableParcelable<NameCard>()
    val createNameCardSuccess = SingleLiveEvent<Void>()

    fun createNameCard(profilePath: String, mediaFilePathsList: ArrayList<String>,
                       logoFilePathsList: ArrayList<String>, isMyNameCard: Boolean, frontPath: String, backPath: String) {
        nameCardRepository.createNameCard(nameCard.get(), createNameCardSuccess, profilePath, mediaFilePathsList, logoFilePathsList, isMyNameCard, frontPath, backPath)
    }

    fun updateNameCard(profilePath: String, mediaFilePathsList: ArrayList<String>,
                       logoFilePathsList: ArrayList<String>, isMyNameCard: Boolean, frontPath: String, backPath: String) {
        nameCardRepository.updateNameCard(nameCard.get(), createNameCardSuccess, profilePath, mediaFilePathsList, logoFilePathsList, isMyNameCard, frontPath, backPath)
    }
}