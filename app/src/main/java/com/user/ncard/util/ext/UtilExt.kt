package com.user.ncard.util.ext

import android.content.Context
import android.support.v4.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Created by Pham on 19/9/17.
 */
fun Context.getColorFromResId(resId: Int) = ContextCompat.getColor(this, resId)
