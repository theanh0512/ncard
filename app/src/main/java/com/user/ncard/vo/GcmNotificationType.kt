package com.user.ncard.vo

/**
 * Created by dangui on 10/11/17.
 */
enum class GcmNotificationType(val type : String) {
    NC_CREATE_FRIEND_REQUEST("nc_create_friend_request"),
    NC_ACCEPT_FRIEND_REQUEST("nc_accept_friend_request"),
    NC_DELETE_FRIEND("nc_delete_friend"),
    NC_SHARE_FRIEND("nc_share_friend"),
    NC_SHARE_CARD("nc_share_card"),
    NC_WALLET_TRANSFERED_EXPIRED("nc_wallet_transfered_expired"),
    NC_WALLET_TRANSFER_EXPIRED("nc_wallet_transfer_expired"),
    NC_GIFT_TRANSFERED_EXPIRED("nc_gift_transfered_expired"),
    NC_GIFT_TRANSFER_EXPIRED("nc_gift_transfer_expired"),
    NC_WITHDRAW_ACCEPTED("nc_withdraw_accepted"),
    NC_WITHDRAW_REJECTED("nc_withdraw_rejected")
}