package com.user.ncard.ui.me.gift

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.GlideHelper


/**
 * Created by Trong on 10/26/16.
 */
class ProductImagesPagerAdapter(val context: Context, val listImages: List<String>) : android.support.v4.view.PagerAdapter() {


    internal var views = SparseArray<View>()

    override fun getCount(): Int {
        return listImages.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        var view: View? = view as View
        (container as ViewPager).removeView(view)
        views.remove(position)
        view = null
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(context).inflate(R.layout.item_image, null) as RelativeLayout
        layout.tag = position
        val imv = layout.findViewById<View>(R.id.imvImage) as ImageView

        if (listImages?.size > position) {
            GlideHelper.displayRaw(imv, listImages[position])
        }

        (container as ViewPager).addView(layout)
        views.put(position, layout)
        return layout
    }
}
