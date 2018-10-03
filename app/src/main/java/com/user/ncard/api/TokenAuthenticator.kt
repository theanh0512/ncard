package com.user.ncard.api

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.lang.Exception

/**
 * Created by trong-android-dev on 23/1/18.
 * // TODO: when having multiple request concurrently, it calls a lot refresh token functions
 */
class TokenAuthenticator(val sharedPreferenceHelper: SharedPreferenceHelper, val context: Context) : Authenticator {

    var count = 0

    override fun authenticate(route: Route?, response: Response?): Request? {
        var token = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_SESSION)
        if (responseCount(response) >= 2) {
            // Do nothing
            return null
        } else {
            // Refresh your access_token using a synchronous api request
            Log.e("NCard", "TokenAuthenticator authenticate")
            Utils.getToken(sharedPreferenceHelper, null)
        }

        return response?.request()?.newBuilder()?.addHeader("Authorization", token)?.build();
    }

    fun responseCount(response: Response?): Int {
        var response = response
        var result = 1
        if (response != null) {
            response = response?.priorResponse()
            while (response != null) {
                result++
                response = response?.priorResponse()
            }
        }
        return result
    }
}