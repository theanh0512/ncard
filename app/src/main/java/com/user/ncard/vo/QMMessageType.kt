package com.user.ncard.vo

/**
 * Created by dangui on 23/11/17.
 *
 * just follow iOS implementation
 */
enum class QMMessageType(val type : Int) {
    normal(0),
    createGroupDialog(1),
    updateGroupDialog(2),
    contactRequest(4),
    acceptContactRequest(-1),
    rejectContactRequest(-1),
    deleteContactRequest(-1);

}