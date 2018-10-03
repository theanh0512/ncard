package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.user.ncard.AppExecutors
import com.user.ncard.SingleLiveEvent
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.api.NCardService
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.vo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class WalletRepository @Inject constructor(val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                           val nCardService: NCardService, val appExecutors: AppExecutors,
                                           val sharedPreferenceHelper: SharedPreferenceHelper) {
    fun getWalletInfo(): LiveData<WalletInfo> {
        var result: MutableLiveData<WalletInfo> = MediatorLiveData<WalletInfo>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getWalletInfo()
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

    fun getEphemeralKey(): LiveData<ResponseBody> {
        var result: MutableLiveData<ResponseBody> = MediatorLiveData<ResponseBody>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val data = nCardService.getEphemeralKey()
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

    fun postDepositLog(depositTransaction: DepositTransaction, transactionResponse: MutableLiveData<TransactionResponse>, errorResponse: MutableLiveData<Throwable>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.postTopUpTransaction(depositTransaction)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            transactionResponse.value = response
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    errorResponse.value = error
                                    Log.e(TAG, "Unable to get response", error)
                                    compositeDisposable.dispose()
                                })
                )
            }
        })
    }

    fun createPaymentPassword(paymentPassword: String, createPaymentPasswordSuccess: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.createWalletPassword(WalletPassword(paymentPassword))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.e(TAG, "Successfully create payment password")
                            createPaymentPasswordSuccess.call()
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    compositeDisposable.dispose()
                                    Log.e(TAG, "Unable to create payment password", error)
                                })
                )
            }
        })
    }

    fun changePaymentPassword(oldPassword: String, newPassword: String, updatePasswordSuccess: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.changeWalletPassword(ChangePassword(oldPassword, newPassword))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.e(TAG, "Successfully change payment password")
                            updatePasswordSuccess.call()
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    compositeDisposable.dispose()
                                    Log.e(TAG, "Unable to change payment password", error)
                                })
                )
            }
        })
    }

    fun forgetPaymentPassword(forgetPasswordResponse: MutableLiveData<ForgetPasswordResponse>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.forgetWalletPassword(ForgetPassword(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            forgetPasswordResponse.value = response
                            Log.e(TAG, "Successfully send forget payment password")
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    compositeDisposable.dispose()
                                    Log.e(TAG, "Unable to send forget payment password", error)
                                })
                )
            }
        })
    }

    fun transferCash(friend: Friend, amount: Float, note: String, password: String, transferCreditResponse: MutableLiveData<TransferCreditResponse>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.postTransfer(RequestTransfer(friend.id, amount, note, password))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            transferCreditResponse.value = response
                            Log.e(TAG, "Successfully send cash")
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    compositeDisposable.dispose()
                                    Log.e(TAG, "Unable to send cash", error)
                                })
                )
            }
        })
    }

    fun getTransactionLogs(page: Int): LiveData<TransactionLogResponse> {
        var result: MutableLiveData<TransactionLogResponse> = MediatorLiveData<TransactionLogResponse>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getTransactionLogs(page)
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

    fun getTransactionLogDetail(transactionId: Int): LiveData<TransactionLogDetail> {
        var result: MutableLiveData<TransactionLogDetail> = MediatorLiveData<TransactionLogDetail>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getTransactionLogDetail(transactionId)
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

    fun withdrawCash(withdrawRequest: WithdrawRequest, withdrawSuccess: SingleLiveEvent<Void>, errorResponse: MutableLiveData<Throwable>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.withdrawToBank(withdrawRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Log.e(TAG, "Successfully withdrawToBank")
                            withdrawSuccess.call()
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    errorResponse.value = error
                                    compositeDisposable.dispose()
                                    Log.e(TAG, "Unable to withdrawToBank", error)
                                })
                )
            }
        })
    }

    fun getEwalletSetting(ewalletSettingResponse: MutableLiveData<EwalletSetting>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                compositeDisposable.add(nCardService.getEwalletSettings()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            Log.e(TAG, "Successfully getEwalletSetting")
                            ewalletSettingResponse.value = response
                            compositeDisposable.dispose()
                        },
                                { error ->
                                    compositeDisposable.dispose()
                                    Log.e(TAG, "Unable to getEwalletSetting", error)
                                })
                )
            }
        })
    }

    companion object {
        private val TAG = "NCard"
    }
}