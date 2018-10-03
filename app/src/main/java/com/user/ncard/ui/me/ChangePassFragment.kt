package com.user.ncard.ui.me

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.amazonaws.AmazonServiceException
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.user.ncard.R
import com.user.ncard.databinding.FragmentChangePassBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.ui.catalogue.utils.Functions
import com.user.ncard.util.AppHelper
import com.user.ncard.vo.User
import java.lang.Exception

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ChangePassFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance(): ChangePassFragment = ChangePassFragment()
    }

    lateinit var viewModel: MeViewModel
    lateinit var fragmentBinding: FragmentChangePassBinding

    override fun getLayout(): Int {
        return R.layout.fragment_change_pass
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.change_pass_title), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MeViewModel::class.java)
        fragmentBinding = FragmentChangePassBinding.bind(rootView)
        // Do with extra data here
    }

    override fun init() {
        // Load data here
        viewModel.getMe()

        viewModel.userData.observe(this, Observer<User> { user ->
            //
        })

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val item = menu?.findItem(R.id.menu_item_do)
        item?.setTitle(R.string.done)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_do_sth, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_do -> {
                consume {
                    if (fragmentBinding.edtOldPass.text.toString().isNotEmpty() && fragmentBinding.edtNewPass.text.toString().isNotEmpty()) {

                        showProgressDialog()
                        val user: CognitoUser? = AppHelper.getPool().currentUser
                        user?.changePasswordInBackground(fragmentBinding.edtOldPass.text.toString(),
                                fragmentBinding.edtNewPass.text.toString(), object : GenericHandler {
                            override fun onSuccess() {
                                hideProgressDialog()
                                Functions.showToastShortMessage(activity, getString(R.string.change_password_successfully))
                            }

                            override fun onFailure(exception: Exception?) {
                                hideProgressDialog()
                                var str = exception?.message
                                if(exception is AmazonServiceException) {
                                    str = exception.errorMessage
                                }
                                exception?.printStackTrace()
                                Functions.showAlertDialog(activity, "", str)
                            }
                        })
                    } else {
                        Functions.showToastShortMessage(activity, "Please input old and new password")
                    }
                }
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onRefresh() {
    }

}