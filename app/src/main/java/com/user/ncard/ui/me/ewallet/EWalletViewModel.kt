package com.user.ncard.ui.me.ewallet

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.util.Log
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.WalletRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import okhttp3.ResponseBody
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class EWalletViewModel @Inject constructor(val walletRepository: WalletRepository,
                                           val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val walletInfo: LiveData<WalletInfo>
    val start = MutableLiveData<Boolean>()
    val startLoadTransactionLog = MutableLiveData<Boolean>()
    val startLoadTransactionLogDetail = MutableLiveData<Boolean>()

    val response: LiveData<ResponseBody>
    var page: Int = DEFAULT_PAGE
    var pagination: PaginationTransactionLog? = null
    val transactionLogResponse: LiveData<TransactionLogResponse>
    val transactionLogDetail: LiveData<TransactionLogDetail>
    var transactionId: Int = DEFAULT_PAGE

    val errorResponse = MutableLiveData<Throwable>()
    val ewalletSettingResponse = MutableLiveData<EwalletSetting>()
    val transactionResponse = MutableLiveData<TransactionResponse>()
    val createPaymentPasswordSuccess = SingleLiveEvent<Void>()
    val transferCreditResponse = MutableLiveData<TransferCreditResponse>()
    val changePasswordSuccess = SingleLiveEvent<Void>()
    val forgetPasswordResponse = MutableLiveData<ForgetPasswordResponse>()

    var amount: ObservableField<String> = ObservableField()
    var bank: ObservableField<String> = ObservableField()
    var name: ObservableField<String> = ObservableField()
    var accountNumber: ObservableField<String> = ObservableField()
    var enableConfirm = ObservableBoolean(false)

    val withdrawSuccess = SingleLiveEvent<Void>()

    private val possibilityUpdateCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            enableConfirm.set(isPossible())
        }
    }

    init {
        walletInfo = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<WalletInfo>()
            } else {
                return@switchMap walletRepository.getWalletInfo()
            }
        }
        response = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<ResponseBody>()
            } else {
                return@switchMap walletRepository.getEphemeralKey()
            }
        }
        transactionLogResponse = Transformations.switchMap(startLoadTransactionLog) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<TransactionLogResponse>()
            } else {
                return@switchMap walletRepository.getTransactionLogs(page)
            }
        }
        transactionLogDetail = Transformations.switchMap(startLoadTransactionLogDetail) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<TransactionLogDetail>()
            } else {
                return@switchMap walletRepository.getTransactionLogDetail(transactionId)
            }
        }
        amount.addOnPropertyChangedCallback(possibilityUpdateCallback)
        name.addOnPropertyChangedCallback(possibilityUpdateCallback)
        accountNumber.addOnPropertyChangedCallback(possibilityUpdateCallback)
        bank.addOnPropertyChangedCallback(possibilityUpdateCallback)
    }

    private fun isPossible(): Boolean {
        return !amount.get().isNullOrEmpty() && !name.get().isNullOrEmpty() && !accountNumber.get().isNullOrEmpty() && !bank.get().isNullOrEmpty()
    }

    fun postDepositTransaction(depositTransaction: DepositTransaction) {
        walletRepository.postDepositLog(depositTransaction, transactionResponse, errorResponse)
    }

    fun createPaymentPassword(walletPassword: String) {
        walletRepository.createPaymentPassword(walletPassword, createPaymentPasswordSuccess)
    }

    fun changePaymentPassword(oldPassword: String, newPassword: String) {
        walletRepository.changePaymentPassword(oldPassword, newPassword, changePasswordSuccess)
    }

    fun forgetPaymentPassword() {
        walletRepository.forgetPaymentPassword(forgetPasswordResponse)
    }

    fun transferCash(friend: Friend, amount: Float, note: String, password: String) {
        walletRepository.transferCash(friend, amount, note, password, transferCreditResponse)
    }

    fun getEwalletSetting() {
        walletRepository.getEwalletSetting(ewalletSettingResponse)
    }

    fun loadMore() {
        page++

        startLoadTransactionLog.value = true
    }

    fun canLoadMore(): Boolean {
        pagination?.nextPage.toString()
        if (pagination != null && pagination?.nextPage != null && pagination?.nextPage != 0
                && pagination?.nextPage.toString() != "") {
            Log.d("NCard", "page current " + page)
            return true
        }
        return false
    }

    fun withdrawCash(walletPassword: String?) {
        walletRepository.withdrawCash(WithdrawRequest(bank.get()!!, amount.get()!!.toDouble(), name.get()!!, accountNumber.get()!!, walletPassword), withdrawSuccess, errorResponse)
    }

    companion object {
        const val DEFAULT_PAGE = 1
    }
}
