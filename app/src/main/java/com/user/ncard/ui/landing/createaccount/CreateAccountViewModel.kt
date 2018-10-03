package com.user.ncard.ui.landing.createaccount

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.user.ncard.R
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.AppHelper
import java.lang.Exception
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class CreateAccountViewModel @Inject constructor(userRepository: UserRepository, val context: Context) : ViewModel() {
    val PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20})"

    val createSuccessEvent = SingleLiveEvent<Void>()

    val email: ObservableField<String> = ObservableField()
    val password: ObservableField<String> = ObservableField()
    val showProgress: ObservableBoolean = ObservableBoolean(false)

    val error: ObservableField<String> = ObservableField()

    fun create() {
        val pattern = Pattern.compile(PATTERN)
        if (password.get().toString().length < 8) error.set(context.getString(R.string.password_less_than_8))
        //else if (!pattern.matcher(password.get().toString()).matches()) error.set(context.getString(R.string.password_invalid_pattern))
        else {
            showProgress.set(true)
            var cognitoUserAttributes = CognitoUserAttributes()
            cognitoUserAttributes.addAttribute("email", email.get()?.toLowerCase())
            AppHelper.getPool().signUpInBackground(email.get()?.toLowerCase(), password.get(), cognitoUserAttributes, null, signUpHandler)
        }
    }

    val signUpHandler = object : SignUpHandler {
        override fun onSuccess(user: CognitoUser?, signUpConfirmationState: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?) {
            showProgress.set(false)
            val regState = signUpConfirmationState
            if (signUpConfirmationState) {
                // User is already confirmed
                error.set("user already confirmed")
            } else {
                // User is not confirmed
                createSuccessEvent.call()
            }
        }

        override fun onFailure(exception: Exception?) {
            showProgress.set(false)
            error.set(AppHelper.formatException(exception))
        }
    }
}