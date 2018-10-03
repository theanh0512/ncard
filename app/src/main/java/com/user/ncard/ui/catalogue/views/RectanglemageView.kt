package com.user.ncard.ui.catalogue.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Created by trong-android-dev on 13/10/17.
 */
class RectanglemageView : ImageView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width / 2)
    }
}