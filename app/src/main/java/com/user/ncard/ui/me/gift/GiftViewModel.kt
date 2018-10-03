package com.user.ncard.ui.me.gift

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.user.ncard.repository.GiftRepository
import com.user.ncard.repository.WalletRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.*
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class GiftViewModel @Inject constructor(val giftRepository: GiftRepository, val walletRepository: WalletRepository,
                                        val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val itemsList: LiveData<List<GiftItem>>
    val giftResponse: LiveData<MyGiftResponse>
    val start = MutableLiveData<Boolean>()
    val getGift = MutableLiveData<Boolean>()
    val getShopItem = MutableLiveData<Boolean>()
    val walletInfo: LiveData<WalletInfo>
    val createOrderResponse = MutableLiveData<CreateOrderResponse>()
    val sendGiftResponse = MutableLiveData<SendGiftResponse>()
    var page = DEFAULT_PAGE
    var pagination: Pagination? = null

    init {
        walletInfo = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<WalletInfo>()
            } else {
                return@switchMap walletRepository.getWalletInfo()
            }
        }
        itemsList = Transformations.switchMap(getShopItem) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<List<GiftItem>>()
            } else {
                return@switchMap giftRepository.getShopItems()
            }
        }
        giftResponse = Transformations.switchMap(getGift) { getGift ->
            if (getGift == null) {
                return@switchMap AbsentLiveData.create<MyGiftResponse>()
            } else {
                return@switchMap giftRepository.getMyGifts()
            }
        }
    }

    fun createOrder(products: List<Product>, walletPassword: String) {
        giftRepository.purchaseGifts(GiftOrderBody(products, walletPassword), createOrderResponse)
    }

    fun sendGift(giftId: Int, sendGiftRequest: SendGiftRequest) {
        giftRepository.sendGift(giftId, sendGiftRequest, sendGiftResponse)
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

    fun loadMore() {
        page++
        getGift.value = true
    }

    companion object {
        private const val DEFAULT_PAGE = 1
    }
}
