package com.user.ncard.ui.catalogue.utils

import com.user.ncard.ui.catalogue.views.RelativeTimeTextView.SERVER_FORMAT
import com.user.ncard.ui.catalogue.views.RelativeTimeTextView.TIME_ZONE
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by trong-android-dev on 3/11/17.
 */
class DateUtils {

    companion object {
        fun convertToDay(dateStr: String): String {
            var formattedDate = ""
            val readFormat = SimpleDateFormat(
                    SERVER_FORMAT)
            readFormat.timeZone = TimeZone.getTimeZone(TIME_ZONE)
            val writeFormat = SimpleDateFormat("dd")
            var date: Date? = null
            try {
                date = readFormat.parse(dateStr)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (date != null) {
                formattedDate = writeFormat.format(date)
            }

            return formattedDate
        }

        fun convertToMonth(dateStr: String): String {
            var formattedDate = ""
            val readFormat = SimpleDateFormat(
                    SERVER_FORMAT)
            readFormat.timeZone = TimeZone.getTimeZone(TIME_ZONE)
            val writeFormat = SimpleDateFormat("MMM")
            var date: Date? = null
            try {
                date = readFormat.parse(dateStr)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (date != null) {
                formattedDate = writeFormat.format(date)
            }

            return formattedDate
        }

        fun convertDateToLong(strReferenceTime: String?): Long? {
            if (strReferenceTime == null) {
                return null
            }
            val f = SimpleDateFormat(SERVER_FORMAT)
            f.timeZone = TimeZone.getTimeZone(TIME_ZONE)
            var d = Date()
            try {
                d = f.parse(strReferenceTime)
            } catch (e: ParseException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            return d.time
        }
    }
}