package com.user.ncard.ui.card.namecard

import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.user.ncard.vo.NameCard

/**
 * Created by Pham on 25/10/17.
 */
class NameCardDetailAdapter(fm: FragmentManager?, val nameCard: NameCard?, val profileUri: Uri?,
                            var logoUriList: ArrayList<ImageSource>?, var mediaUriList: ArrayList<ImageSource>?,
                            val frontUri: Uri?, val backUri: Uri?) : FragmentStatePagerAdapter(fm) {

    var listFragment = ArrayList<Fragment>()

    init {
        listFragment.add(NameCardDetailFragment.newInstance(nameCard, profileUri))
        if ((nameCard?.certUrls != null && nameCard?.certUrls?.isNotEmpty()!!) || (logoUriList!= null && logoUriList?.isNotEmpty()!!)) {
            listFragment.add(NameCardLogoFragment.newInstance(nameCard, logoUriList))
        }
        if (nameCard?.mediaUrls != null && nameCard?.mediaUrls?.isNotEmpty()!! || (mediaUriList!= null && mediaUriList?.isNotEmpty()!!)) {
            listFragment.add(NameCardMediaFragment.newInstance(nameCard, mediaUriList))
        }
        if (!nameCard?.description.isNullOrEmpty()) {
            listFragment.add(NameCardJobFragment.newInstance(nameCard, profileUri))
        }
        if (!nameCard?.frontUrl.isNullOrEmpty() || (frontUri != null)) {
            listFragment.add(NameCardFrontFragment.newInstance(nameCard, frontUri))
        }
        if (!nameCard?.backUrl.isNullOrEmpty() || (backUri != null)) {
            listFragment.add(NameCardBackFragment.newInstance(nameCard, backUri))
        }

    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            position -> listFragment[position]
            else -> listFragment[0]
        }
    }

    override fun getCount(): Int {
        return listFragment.size
    }
}