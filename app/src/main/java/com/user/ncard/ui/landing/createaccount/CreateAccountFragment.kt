package com.user.ncard.ui.landing.createaccount

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentCreateAccountBinding
import com.user.ncard.di.Injectable
import com.user.ncard.ui.landing.confirmaccount.ConfirmAccountFragment
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.getColorFromResId
import javax.inject.Inject

class CreateAccountFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: CreateAccountViewModel
    private lateinit var viewDataBinding: FragmentCreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_create_account, container, false)!!
        Utils.setWindowsWithBackgroundColor(activity, context.getColorFromResId(R.color.colorDarkerWhite))
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CreateAccountViewModel::class.java)
        viewDataBinding.viewmodel = viewModel
        viewModel.apply {
            createSuccessEvent.observe(this@CreateAccountFragment, Observer {
                if (viewModel.email.get() != null && viewModel.password.get() != null) {
                    val confirmAccountFragment = ConfirmAccountFragment.newInstance(viewModel.email.get()?.toLowerCase()!!, viewModel.password.get()!!)
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, confirmAccountFragment)
                            .addToBackStack("ConfirmAccountFragment")
                            .commitAllowingStateLoss()

                }
            })
        }
    }
}
