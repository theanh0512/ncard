package com.user.ncard.api

import android.content.Context
import android.util.Log
import com.user.ncard.util.SharedPreferenceHelper
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


/**
 * Created by Pham on 27/6/17.
 */

class AddCookiesInterceptor(val sharedPreferenceHelper: SharedPreferenceHelper, val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        var cookie = sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_SESSION)
        Log.e("NCard", "token: " + cookie)
        builder.addHeader("Authorization", cookie)
        var response = chain.proceed(builder.build()) //perform request, here original request will be executed
        return response
    }
}
