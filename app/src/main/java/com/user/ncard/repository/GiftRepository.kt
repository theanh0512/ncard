package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.google.gson.Gson
import com.user.ncard.AppExecutors
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.api.NCardService
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.toLiveData
import com.user.ncard.vo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class GiftRepository @Inject constructor(val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                         val nCardService: NCardService, val appExecutors: AppExecutors,
                                         val sharedPreferenceHelper: SharedPreferenceHelper) {
    fun getShopItems(): LiveData<List<GiftItem>> {
        var result: MutableLiveData<List<GiftItem>> = MediatorLiveData<List<GiftItem>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getShopItem()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                data.subscribe({ it ->
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun getMyGifts(): LiveData<MyGiftResponse> {
        val result: MutableLiveData<MyGiftResponse> = MutableLiveData<MyGiftResponse>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getMyGifts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                data.subscribe({ it ->
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun purchaseGifts(body: GiftOrderBody, createOrderResponse: MutableLiveData<CreateOrderResponse>) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.createOrder(body)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            createOrderResponse.value = response
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    val statusAndMessage = Gson().fromJson<StatusAndMessage>((error as HttpException).response().errorBody()?.string(), StatusAndMessage::class.java)
                                    createOrderResponse.value = CreateOrderResponse(0, Balance("", ""), ArrayList(), statusAndMessage)
                                    compositeDisposable.dispose()
                                    Log.e(TAG, "Unable to get response" + error.response().errorBody()?.string())
                                })
                )
            }
        })
    }

    fun sendGift(giftId: Int, body: SendGiftRequest, sendGiftResponse: MutableLiveData<SendGiftResponse>) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.sendGift(giftId, body)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            sendGiftResponse.value = response
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    Log.e(TAG, "Unable to get response", error)
                                    compositeDisposable.dispose()
                                })
                )
            }
        })
    }

    companion object {
        private val TAG = "NCard"
    }
}