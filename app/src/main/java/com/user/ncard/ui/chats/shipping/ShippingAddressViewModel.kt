package com.user.ncard.ui.chats.shipping

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.databinding.ObservableField
import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.user.ncard.repository.BroadcastGroupRepository
import com.user.ncard.repository.ChatRepository
import com.user.ncard.repository.UserRepository
import com.user.ncard.ui.catalogue.BaseViewModel
import com.user.ncard.ui.catalogue.MessageObject
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.ui.catalogue.utils.Pagination
import com.user.ncard.ui.chats.broadcastdetail.BroadcastGroupDetailFragment
import com.user.ncard.ui.chats.utils.ChatConverter
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import javax.inject.Inject

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ShippingAddressViewModel @Inject constructor(val chatRepository: ChatRepository,
                                                   val sharedPreferenceHelper: SharedPreferenceHelper) : BaseViewModel() {

    val attend: ObservableField<String> = ObservableField()
    val contactNumber: ObservableField<String> = ObservableField()
    val diliveryAddress: ObservableField<String> = ObservableField()
    val zipCode: ObservableField<String> = ObservableField()
    val remark: ObservableField<String> = ObservableField()

    var chatMessage: ChatMessage? = null

    init {
        initData()
    }

    override fun initData() {
    }

    fun clickConfirm(view: View) {
        if (attend.get().isNullOrBlank() || contactNumber.get().isNullOrBlank()
                || diliveryAddress.get().isNullOrBlank() || zipCode.get().isNullOrBlank()) {
            Functions.showToastShortMessage(view?.context, "Please input all information")
        } else {
            EventBus.getDefault().post(ShippingEvent(Shipping(attend.get()!!, contactNumber.get()!!, diliveryAddress.get()!!, zipCode.get()!!, remark.get()!!), chatMessage!!))
            (view.context as Activity).finish()
        }
    }

    fun initData(messageId: String?) {
        if (messageId != null) {
            object : AsyncTask<Void, Void, List<ChatDialog>?>() {
                override fun doInBackground(vararg params: Void): List<ChatDialog>? {
                    // get user chatting
                    chatMessage = chatRepository.chatMessageDao.findChatMessage(messageId)
                    return null
                }
            }.execute()
        }
    }


}