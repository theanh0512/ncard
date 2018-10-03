package com.user.ncard.vo

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * Created by dangui on 10/11/17.
 */

@Entity
class EwalletSetting(
        @PrimaryKey var id: Double?,
        var minTopupAmount: Double?,
        var maxTopupAmount: Double?,
        var minTransferAmount: Double?,
        var maxTransferAmount: Double?,
        var minWithdrawAmount: Double?,
        var maxWithdrawAmount: Double?,
        var giftCashOutChargeFee: Double?
) : Serializable