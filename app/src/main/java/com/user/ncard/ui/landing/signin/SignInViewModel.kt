package com.user.ncard.ui.landing.signin

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
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
class SignInViewModel @Inject constructor(val userRepository: UserRepository, context: Context,
                                          val sharedPreferenceHelper: SharedPreferenceHelper,
                                          val chatHelper: ChatHelper) : ViewModel() {
    val email: ObservableField<String>? = ObservableField()
    val password: ObservableField<String>? = ObservableField()
    val newPassword: ObservableField<String>? = ObservableField()
    val retypedNewPassword: ObservableField<String>? = ObservableField()
    val showProgress: ObservableBoolean = ObservableBoolean(false)

    val error: ObservableField<String> = ObservableField()
    val signInSuccessEvent = SingleLiveEvent<Void>()
    val signInFailureEvent = SingleLiveEvent<Void>()
    val userNotConfirmEvent = SingleLiveEvent<Void>()
    val errorForgetPassword = MutableLiveData<String>()
    val forgetPasswordSuccess = SingleLiveEvent<Void>()
    val continueToCode = SingleLiveEvent<Void>()
    var forgetPasswordContinuation: ForgotPasswordContinuation? = null

    var forgetPasswordHandler = object : ForgotPasswordHandler {
        override fun onSuccess() {
            forgetPasswordSuccess.call()
        }

        override fun onFailure(exception: Exception?) {
            if (exception != null) errorForgetPassword.value = AppHelper.formatException(exception)
        }

        override fun getResetCode(continuation: ForgotPasswordContinuation?) {
            forgetPasswordContinuation = continuation
            continueToCode.call()
        }
    }

    fun signIn() {
        showProgress.set(true)
        AppHelper.getPool().getUser(email?.get()?.toLowerCase()).getSessionInBackground(
                Utils.getAuthenticationHandler(sharedPreferenceHelper, signInSuccessEvent,
                        email?.get()?.toLowerCase() ?: "", password?.get() ?: "", error, signInFailureEvent, userNotConfirmEvent))
    }

    fun signInWithEmailAndPassword(email:String, password:String){
        AppHelper.getPool().getUser(email.toLowerCase()).getSessionInBackground(
                Utils.getAuthenticationHandler(sharedPreferenceHelper, signInSuccessEvent,
                        email.toLowerCase(), password, error, signInFailureEvent, userNotConfirmEvent))
    }

    fun getUserInfo(callback: Callback<User>) {
        userRepository.getUserWhenSignIn(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USERNAME), callback)
    }

    fun forgetPassword(email: String) {
        AppHelper.getPool().getUser(email).forgotPasswordInBackground(forgetPasswordHandler)
    }
}