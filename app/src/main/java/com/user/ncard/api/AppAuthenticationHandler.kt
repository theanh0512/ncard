package com.user.ncard.api

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession

/**
 * Created by trong-android-dev on 13/2/18.
 */
interface AppAuthenticationHandler {

    fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
    }
}