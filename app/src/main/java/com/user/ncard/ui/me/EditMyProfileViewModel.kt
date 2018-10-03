package com.user.ncard.ui.me

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableParcelable
import android.util.Log
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.User
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class EditMyProfileViewModel @Inject constructor(val userRepository: UserRepository,
                                                 val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val user = ObservableParcelable<User>()
    val updateInfoSuccess = SingleLiveEvent<Void>()

    fun updateUser(profilePath: String, birthday: String?) {
        if (!birthday.isNullOrEmpty()) {
            val currentFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val uploadFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            try {
                val d = currentFormat.parse(birthday)
                user.get()?.birthday = uploadFormat.format(d)
            } catch (ex: ParseException) {
                Log.e("NCard", "Unable to parse date")
            }
        }
        if (user.get() != null)
            userRepository.updateMyInformation(user.get()!!, updateInfoSuccess, profilePath)
    }
}