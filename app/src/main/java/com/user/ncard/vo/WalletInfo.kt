package com.user.ncard.vo

/**
 * Created by Pham on 11/9/17.
 */
data class WalletInfo(val balance: Balance, val status: String, val walletPassword: String?)

data class Balance(val currency: String, val amount: String)

data class WalletPassword(val walletPassword: String)

data class ChangePassword(val oldPassword: String, val newPassword: String)

data class ForgetPassword(val email: String)

data class ForgetPasswordResponse(val tempPassword: String, val tempPasswordExpiry: String)