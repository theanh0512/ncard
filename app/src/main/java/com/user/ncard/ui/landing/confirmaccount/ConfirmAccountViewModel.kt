package com.user.ncard.ui.landing.confirmaccount

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.AppHelper
import com.user.ncard.util.ChatHelper
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.User
import retrofit2.Callback
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class ConfirmAccountViewModel @Inject constructor(val userRepository: UserRepository, context: Context,
                                                  val sharedPreferenceHelper: SharedPreferenceHelper,
                                                  val chatHelper: ChatHelper) : ViewModel() {
    val code: ObservableField<String> = ObservableField()
    var username: ObservableField<String> = ObservableField()
    var password: ObservableField<String> = ObservableField()
    var confirmEvent: SingleLiveEvent<Void> = SingleLiveEvent()
    var signInEvent: SingleLiveEvent<Void> = SingleLiveEvent()
    var signInFailureEvent: SingleLiveEvent<Void> = SingleLiveEvent()
    val error: ObservableField<String> = ObservableField()
    val confirmResend: SingleLiveEvent<Void> = SingleLiveEvent()
    val wrongCodeEvent: SingleLiveEvent<Void> = SingleLiveEvent()
    val userNotConfirmEvent = SingleLiveEvent<Void>()
    val showProgress: ObservableBoolean = ObservableBoolean(false)

    fun submit() {
        if (code.get()?.length == 6) {
            showProgress.set(true)
            AppHelper.getPool().getUser(username.get()).confirmSignUpInBackground(code.get(), true, confHandler)
        }
    }

    fun resend() {
        AppHelper.getPool().getUser(username.get()).resendConfirmationCodeInBackground(verificationHandler)
    }

    var confHandler: GenericHandler = object : GenericHandler {
        override fun onSuccess() {
            Log.e("NCard", "Confirmed")
            confirmEvent.call()
        }

        override fun onFailure(exception: Exception) {
            wrongCodeEvent.call()
            Log.e("NCard", "Failed to confirm " + AppHelper.formatException(exception))
        }
    }

    var verificationHandler: VerificationHandler = object : VerificationHandler {
        override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails?) {
            confirmResend.call()
        }

        override fun onFailure(exception: Exception?) {
            Log.e("NCard", "Failed to resend " + AppHelper.formatException(exception))
        }
    }

    fun signIn() {
        if (username.get() != null && password.get() != null) {
            AppHelper.getPool().getUser(username.get()).getSessionInBackground(
                    Utils.getAuthenticationHandler(sharedPreferenceHelper, signInEvent, username.get()!!, password.get()!!, error, signInFailureEvent, userNotConfirmEvent))
        }
    }

    fun getUserInfo(callback: Callback<User>) {
        userRepository.getUserWhenSignIn(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USERNAME), callback)
    }

    fun getUser() {
        userRepository.getUserByUserName(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL))
    }
}