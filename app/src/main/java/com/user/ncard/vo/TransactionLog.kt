package com.user.ncard.vo

import android.arch.persistence.room.Embedded
import com.google.gson.annotations.SerializedName

/**
 * Created by Pham on 21/12/17.
 */
data class DepositTransaction(val provider: String, val amount: Int, val currency: String, val source: String)

data class WithdrawRequest(val bankName: String, val amount: Double, val bankAccountName: String, val bankAccountId: String, val walletPassword: String?, val currency: String = "SGD")

data class TransactionResponse(val status: String)

data class TransferCreditResponse(val id: Int, val title: String, @SerializedName("amount") @Embedded(prefix = "credit_amount_") val amount: AmountBalance, val type: String,
                                  var status: String, val createdAt: String, val receiptTime: String?, val paymentMethod: String, val paymentMethodInfo: String,
                                  @SerializedName("sender") @Embedded(prefix = "credit_sender_") val sender: SenderReceiver,
                                  @SerializedName("receiver") @Embedded(prefix = "credit_receiver_") val receiver: SenderReceiver, @SerializedName("balance") @Embedded(prefix = "credit_balance_") val balance: AmountBalance?) {

    class AmountBalance(val currency: String, val amount: Double?)

    class SenderReceiver(val id: Int, val name: String, val profileImageUrl: String?)
}

enum class EWalletTransactionStatusType(val status: String) {
    COMPLETED("succeeded"),
    FAILED("failed"),
    REFUNDED("refunded"),
    ONHOLD("on_hold")
}

enum class EWalletTransactionAction(val action: String) {
    ACCEPT("accept"),
    REJECT("reject")
}

data class RequestTransfer(val recipientId: Int, val amount: Float, val message: String, val walletPassword: String)

data class RequestUpdateCreditTransaction(val action: String)

data class TransactionLog(val id: Int, val title: String, val amount: Price, val type: String, val status: String, val createdAt: String, val receiptTime: String?)

data class TransactionLogResponse(val data: List<TransactionLog>, val pagination: PaginationTransactionLog)

data class PaginationTransactionLog(var currentPage: Int, var nextPage: Int)

data class TransactionLogDetail(val id: Int, val title: String, val amount: Price, val type: String?,
                                val status: String, val createdAt: String?, val receiptTime: String?,
                                val sender: TransferCreditResponse.SenderReceiver, val receiver: TransferCreditResponse.SenderReceiver,
                                val paymentMethod: String, val paymentMethodInfo: String)