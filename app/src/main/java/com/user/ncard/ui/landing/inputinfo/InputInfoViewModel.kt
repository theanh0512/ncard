package com.user.ncard.ui.landing.inputinfo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.vo.ExtraSignUpInfo
import com.user.ncard.vo.Resource
import com.user.ncard.vo.User
import java.util.*
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class InputInfoViewModel @Inject constructor(val userRepository: UserRepository, context: Context) : ViewModel() {
    val username: MutableLiveData<String>? = MutableLiveData()
    val user: LiveData<Resource<User>>
    val firstName: ObservableField<String> = ObservableField()
    val lastName: ObservableField<String> = ObservableField()
    val mobile: ObservableField<String> = ObservableField()
    val updateInfoSuccessEvent = SingleLiveEvent<Void>()
    val showProgress: ObservableBoolean = ObservableBoolean(false)

    init {
        user = Transformations.switchMap(username) { username ->
            if (username == null) {
                return@switchMap AbsentLiveData.create<Resource<User>>()
            } else {
                return@switchMap userRepository.getUserByUserName(username)
            }
        }
    }

    fun setUsername(username: String) {
        if (Objects.equals(this.username?.value, username)) {
            return
        }
        this.username?.value = username
    }

    fun update(userId: Int) {
        if (firstName.get() != null && lastName.get() != null && mobile.get() != null) {
            showProgress.set(true)
            val extraSignUpInFo = ExtraSignUpInfo(firstName.get()!!, lastName.get()!!, mobile.get()!!)
            userRepository.updateUserInfoWhenSigningUp(userId, extraSignUpInFo, updateInfoSuccessEvent)
        }
    }
}