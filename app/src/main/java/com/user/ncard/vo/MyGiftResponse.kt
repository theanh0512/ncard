package com.user.ncard.vo

import android.arch.persistence.room.Embedded
import java.io.Serializable

/**
 * Created by Pham on 8/1/18.
 */
data class MyGiftResponse(val purchaseItems: List<MyGiftData>?)

data class MyGiftData(val id: Int, val product: GiftItem, val createdAt: String, val updatedAt: String)

data class CreateOrderResponse(val transactionId: Int, val balance: Balance, val gifts: List<MyGiftData>, val statusAndMessage: StatusAndMessage? = null)

data class StatusAndMessage(val status:String, val message: String) : Serializable

data class GiftOrderBody(val products: List<Product>, val walletPassword: String)

data class Product(val id: Int, val quantity: Int)

data class Pagination(val totalPages: Int, val pageNumber: Int, val total: Int, val pageSize: Int, val currentPage: Int, val nextPage: Int?)

data class SendGiftRequest(val receiverId: Int, val message: String)

data class SendGiftResponse(val id: Int, val purchasedItemId: Int, @Embedded(prefix = "gift_product_") val product: GiftItem?, var status: String,
                            val message: String, @Embedded(prefix = "gift_sender_") val sender: Participant,
                            @Embedded(prefix = "gift_receiver_") val receiver: Participant, val createdAt: String?, val updatedAt: String?) {

    data class GiftItem(val id: Int, val title: String, val description: String, val imageUrl: String, @Embedded(prefix = "gift_price_") val price: Price?,
                        @Embedded(prefix = "gift_rep_rice_") val regularPrice: Price?, @Embedded(prefix = "gift_sale_price_") val salePrice: Price?, val sku: String?, val virtual: Boolean?,
                        val onSale: Boolean?, val featured: Boolean?, val categories: List<Category>?, val imageUrls: List<String>?,
                        val inStock: Boolean?, val stockQuantity: Int?, val vendorName: String, var count: Int?)

    data class Price(val currency: String, val amount: Double?)
    data class Category(val id: Int, val name: String, val slug: String)
}

data class Participant(val id: Int, val name: String, val profileImageUrl: String?)

enum class ECommerceGiftAction(val action: String) {
    ACCEPT("accept"),
    REJECT("reject"),
    TRANSFER("transfer"),
    CASHOUT("cash_out")
}

enum class ECommerceGiftStatus(val status: String) {
    SENT("sent"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CASHED("cashed")
}

enum class ECommerceGiftActionStatus(val status: String) {
    ACCEPT("accept"),
    CASHOUT("cashout"),
    SAVE("save"),
}

data class Shipping(val fullName: String, val phone: String, val address: String?, val postcode: String, val remark: String)

data class RequestUpdateGift(val action: String, val receiverId: String?, val message: String, val shipping: Shipping?)