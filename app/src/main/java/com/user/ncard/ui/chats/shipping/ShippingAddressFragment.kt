package com.user.ncard.ui.chats.shipping

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import com.user.ncard.R
import com.user.ncard.databinding.FragmentShippingAddressBinding
import com.user.ncard.ui.catalogue.BaseFragment

/**
 * Created by trong-android-dev on 22/11/17.
 */

class ShippingAddressFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance(): ShippingAddressFragment = ShippingAddressFragment()
    }

    lateinit var viewModel: ShippingAddressViewModel
    lateinit var fragmentBinding: FragmentShippingAddressBinding

    override fun getLayout(): Int {
        return R.layout.fragment_shipping_address
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initToolbar(getString(R.string.title_delivery_address), true)
    }

    override fun initBinding() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ShippingAddressViewModel::class.java)
        fragmentBinding = FragmentShippingAddressBinding.bind(rootView)
        fragmentBinding.viewModel = viewModel
        // Do with extra data here
        val bundle = activity.intent.extras
        val messageId = bundle.getString("messageId")
        viewModel.initData(messageId)
    }

    override fun init() {
        // Load data here
    }

    override fun onRefresh() {
    }

}