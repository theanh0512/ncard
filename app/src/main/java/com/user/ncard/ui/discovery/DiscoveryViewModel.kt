package com.user.ncard.ui.discovery

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.user.ncard.repository.UserRepository
import com.user.ncard.util.SharedPreferenceHelper
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class DiscoveryViewModel @Inject constructor(val userRepository: UserRepository, context: Context, val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {

}