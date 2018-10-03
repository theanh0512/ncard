package com.user.ncard.ui.landing.inputinfo

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.MainActivity
import com.user.ncard.R
import com.user.ncard.databinding.FragmentInputInfoBinding
import com.user.ncard.di.Injectable
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import com.user.ncard.vo.Resource
import com.user.ncard.vo.User
import javax.inject.Inject

class InputInfoFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    lateinit var viewModel: InputInfoViewModel
    private lateinit var viewDataBinding: FragmentInputInfoBinding
    lateinit var progressingAlert: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_input_info, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(InputInfoViewModel::class.java)

        //load user information
        viewDataBinding.apply {
            viewmodel = viewModel
            buttonSkipForNow.isEnabled = false
            buttonSkipForNow.setTextColor(activity.getColorFromResId(R.color.colorDarkGrey))
            buttonSubmit.isEnabled = false
            buttonSubmit.setBackgroundResource(R.drawable.rounded_corner_button_disabled)
        }
        progressingAlert = Utils.showAlert(activity)

        viewModel.setUsername(sharedPreferenceHelper.getString(SharedPreferenceHelper.Key.CURRENT_USER_EMAIL))
        viewModel.user.observe(this, Observer<Resource<User>> { resource ->
            if (resource?.data != null) {
                progressingAlert.cancel()
                viewDataBinding.apply {
                    user = resource.data
                    buttonSkipForNow.isEnabled = true
                    buttonSkipForNow.setTextColor(activity.getColorFromResId(R.color.colorDarkBlue))
                    buttonSubmit.isEnabled = true
                    buttonSubmit.setBackgroundResource(R.drawable.rounded_corner_button)
                }
            }
        })
        viewModel.apply {
            updateInfoSuccessEvent.observe(this@InputInfoFragment, Observer {
                showProgress.set(false)
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity.finish()
            })
        }
        viewDataBinding.buttonSkipForNow.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity.finish()
        }
    }
}