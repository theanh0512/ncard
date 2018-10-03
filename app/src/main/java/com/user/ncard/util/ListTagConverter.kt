package com.user.ncard.util

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.user.ncard.vo.CataloguePost
import com.user.ncard.vo.Job

/**
 * Created by Pham on 12/10/17.
 */

class ListTagConverter {
    @TypeConverter
    fun fromTagString(value: String): List<CataloguePost.Tag>? {
        if(value?.isNotEmpty()) {
            val array = value.split("_|_")
            val gson = Gson()
            val list = (0 until array.size).mapTo(ArrayList()) { gson.fromJson(array[it], CataloguePost.Tag::class.java) }
            return list
        }
        return arrayListOf()
    }

    @TypeConverter
    fun fromTagArrayList(list: List<CataloguePost.Tag>?): String {
        if (list != null && list.size > 0) {
            val array = arrayOfNulls<CataloguePost.Tag>(list.size)
            for (i in 0 until list.size) {
                array[i] = list[i]
            }
            var str = ""
            val gson = Gson()
            for (i in array.indices) {
                val jsonString = gson.toJson(array[i])
                str += jsonString
                if (i < array.size - 1) {
                    str += "_|_"
                }
            }
            return str
        }
        return ""
    }
}