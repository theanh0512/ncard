package com.user.ncard.ui.card.namecard

import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.user.ncard.R
import com.user.ncard.databinding.FragmentNameCardDetailBinding
import com.user.ncard.vo.NameCard

class NameCardDetailFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentNameCardDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_name_card_detail, container, false)!!
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDataBinding.namecard = arguments.getParcelable(ARGUMENT_NAME_CARD)
        val profileUri = arguments.getParcelable<Uri>(ARGUMENT_PROFILE_URI)
        if (profileUri != null)
            Glide.with(activity).load(arguments.getParcelable(ARGUMENT_PROFILE_URI)).into(viewDataBinding.imageViewProfile)
        else Glide.with(activity).load(viewDataBinding.namecard.companyLogoUrl).into(viewDataBinding.imageViewProfile)
    }

    companion object {
        private const val ARGUMENT_NAME_CARD = "NAME_CARD"
        private const val ARGUMENT_PROFILE_URI = "PROFILE_URI"

        fun newInstance(nameCard: NameCard?, profileUri: Uri?) = NameCardDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARGUMENT_NAME_CARD, nameCard)
                putParcelable(ARGUMENT_PROFILE_URI, profileUri)
            }
        }

    }
}