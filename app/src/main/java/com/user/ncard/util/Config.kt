package com.user.ncard.util

import com.user.ncard.BuildConfig

/**
 * Created by Pham on 20/12/17.
 */
object Config {
    val STRIPE_KEY: String
        get() {
            return if (BuildConfig.DEBUG  && Constants.DEV) "pk_test_NHuQP2PJbDuMElxIjLWEfAXk"
            else "pk_live_bG5YCdrDQk2Vjnu5w9CM8QL8"
        }
}