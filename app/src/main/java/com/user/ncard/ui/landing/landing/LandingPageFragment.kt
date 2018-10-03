package com.user.ncard.ui.landing.landing

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.user.ncard.R
import com.user.ncard.databinding.FragmentLandingPageBinding
import com.user.ncard.ui.landing.LandingPageActivity
import com.user.ncard.util.Utils

/**
 * A placeholder fragment containing a simple view.
 */
class LandingPageFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentLandingPageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentLandingPageBinding.inflate(inflater, container, false)
        Utils.setWindowsWithBackgroundDrawable(activity, R.drawable.bg_blue)
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.apply { viewmodel = (activity as LandingPageActivity).viewModel }
    }
}
