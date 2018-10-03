package com.user.ncard.ui.me

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import com.user.ncard.R
import com.user.ncard.databinding.FragmentAccountBinding
import com.user.ncard.ui.catalogue.BaseFragment
import com.user.ncard.vo.User

/**
 * Created by trong-android-dev on 22/11/17.
 */

class AccountFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance(): AccountFragment = AccountFragment()
    }

    lateinit var viewModel: MeViewModel
    lateinit var fragmentBinding: FragmentAccountBinding

    override fun getLayout(): Int {
        return R.layout.fragment_account
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.account), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MeViewModel::class.java)
        fragmentBinding = FragmentAccountBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
    }

    override fun init() {
        // Load data here
        viewModel.getMe()

        viewModel.userData.observe(this, Observer<User> { user ->
            fragmentBinding.user = user
        })

        fragmentBinding.rlChangPass.setOnClickListener({
            startActivity(ChangePassActivity.getIntent(context))
        })
    }

    override fun onRefresh() {
    }

}