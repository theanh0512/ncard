package com.user.ncard.ui.card.catalogue

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.user.ncard.R
import com.user.ncard.ui.catalogue.utils.SpannableBuilder

/**
 * Created by trong-android-dev on 16/10/17.
 */
class ViewUtils {
    companion object {

        @SuppressLint("ResourceAsColor")
        fun buildCommentCatalogue(context: Context, name: String?, comment: String): View {
            val layout = (context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.item_catalogue_comment_main, null, false) as LinearLayout
            val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, 0, 0, 0)
            layout.layoutParams = layoutParams

            val tvName = layout.findViewById<View>(R.id.tvName) as TextView
            val tvComment = layout.findViewById<View>(R.id.tvComment) as TextView

            val item = SpannableBuilder(context)
                    .createStyle().setColorResId(R.color.colorDarkBlue).apply()
                    .append(name)
                    .clearStyle()
                    .append(": " + comment)
                    .build()
            /*name?.let { tvName.text = name }
            tvComment.text = ": " + comment*/
            tvName.visibility = View.GONE
            tvComment.text = item

            return layout
        }

        fun buildTagsCatalogue(context: Context, name: String?): View {
            val layout = (context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.item_tag, null, false) as RelativeLayout
            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(0, 0, 0, 5)
            layout.layoutParams = layoutParams

            val tvName = layout.findViewById<View>(R.id.tvTagName) as TextView

            tvName.text = name

            return layout
        }
    }
}