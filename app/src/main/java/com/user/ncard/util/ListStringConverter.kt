package com.user.ncard.util

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Created by Pham on 12/10/17.
 */

class ListStringConverter {
    @TypeConverter
    fun fromString(value: String): List<String>? {
        val listType = object : TypeToken<List<String>>() {

        }.type
        return Gson().fromJson<List<String>>(value, listType)
    }

    @TypeConverter
    fun fromArrayLisr(list: List<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
