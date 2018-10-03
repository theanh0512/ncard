package com.user.ncard.ui.card.namecard

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.util.Log
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.NameCard
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class SetRemarkNameCardViewModel @Inject constructor(private val nameCardRepository: NameCardRepository,
                                                     val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val successEvent = SingleLiveEvent<Void>()
    val remark: ObservableField<String> = ObservableField()
    val gender: ObservableField<String> = ObservableField()
    val industry: ObservableField<String> = ObservableField()
    val nationality: ObservableField<String> = ObservableField()
    val country: ObservableField<String> = ObservableField()
    val birthday: ObservableField<String> = ObservableField()

    fun updateRemark(nameCard: NameCard) {
        if (!birthday.get().isNullOrEmpty()) {
            val currentFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val uploadFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            try {
                val d = currentFormat.parse(birthday.get())
                nameCard.birthday = uploadFormat.format(d)
            } catch (ex: ParseException) {
                Log.e("NCard", "Unable to parse date")
            }
        }
        nameCard.apply {
            remark = this@SetRemarkNameCardViewModel.remark.get() ?: ""
            gender = this@SetRemarkNameCardViewModel.gender.get() ?: ""
            industry = this@SetRemarkNameCardViewModel.industry.get() ?: ""
            nationality = this@SetRemarkNameCardViewModel.nationality.get() ?: ""
            country = this@SetRemarkNameCardViewModel.country.get() ?: ""
        }
        nameCardRepository.updateNameCardRemark(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), nameCard, successEvent)
    }
}