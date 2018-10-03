package com.user.ncard.ui.catalogue.utils

/**
 * Created by CONCARO on 10/30/2015.
 */
interface FragmentListener {

    fun showProgressDialog()

    fun hideProgressDialog()

    fun showSnackbarMessage(error: String?)

    fun showToastMessage(toast: String?)

}
