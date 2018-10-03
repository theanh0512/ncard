package com.user.ncard.ui.landing.landing

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.UserRepository
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class LandingPageViewModel @Inject constructor(userRepository: UserRepository, context: Context) : ViewModel() {


    val email: ObservableField<String> = ObservableField()
    val password: ObservableField<String> = ObservableField("Password")
    val showProgress: ObservableBoolean = ObservableBoolean(false)

    val createNewAccountEvent = SingleLiveEvent<Void>()
    val signInEvent = SingleLiveEvent<Void>()

    fun createNewAccount() {
        createNewAccountEvent.call()
    }

    fun signIn(){
        signInEvent.call()
    }

//    fun create() {
//        showProgress.set(true)
//        AppHelper.getPool().signUpInBackground(email.get(), password.get(), null, null, signUpHandler)
//    }
//
//    val signUpHandler = object : SignUpHandler {
//        override fun onSuccess(user: CognitoUser?, signUpConfirmationState: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?) {
//            showProgress.set(false)
//            val regState = signUpConfirmationState
//            if (signUpConfirmationState) {
//                // User is already confirmed
//                password.set("user already confirmed")
//            } else {
//                // User is not confirmed
//                password.set(cognitoUserCodeDeliveryDetails?.destination)
//            }
//        }
//
//        override fun onFailure(exception: Exception?) {
//            showProgress.set(false)
//            password.set(exception.toString())
//        }
//}
}