package com.user.ncard.util

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.user.ncard.vo.*
import java.util.*


/**
 * Created by Pham on 12/10/17.
 */

class Converter {
    @TypeConverter
    fun fromJobString(value: String): List<Job>? {
        val jobArray = value.split("_|_")
        val gson = Gson()
        val list = (0 until jobArray.size).mapTo(ArrayList()) { gson.fromJson(jobArray[it], Job::class.java) }
        return list
    }

    @TypeConverter
    fun fromJobArrayList(list: List<Job>?): String {
        if (list != null) {
            val videoArray = arrayOfNulls<Job>(list.size)
            for (i in 0 until list.size) {
                videoArray[i] = list[i]
            }
            var str = ""
            val gson = Gson()
            for (i in videoArray.indices) {
                val jsonString = gson.toJson(videoArray[i])
                str += jsonString
                if (i < videoArray.size - 1) {
                    str += "_|_"
                }
            }
            return str
        }
        return ""
    }

    @TypeConverter
    fun fromString(value: String): ArrayList<String>? {
        val listType = object : TypeToken<ArrayList<String>>() {

        }.type
        return Gson().fromJson<ArrayList<String>>(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String>?): String {
        return if (list != null) {
            val gson = Gson()
            gson.toJson(list)
        } else ""
    }

    @TypeConverter
    fun fromGroupItemString(value: String): List<GroupItem>? {
        val groupItemArray = value.split("_|_")
        val gson = Gson()
        val list = (0 until groupItemArray.size).mapTo(ArrayList()) {
            gson.fromJson(groupItemArray[it], GroupItem::class.java)
        }
        return list
    }

    @TypeConverter
    fun fromGroupItemArrayList(list: List<GroupItem>?): String {
        if (list != null) {
            val groupItemArray = arrayOfNulls<GroupItem>(list.size)
            for (i in 0 until list.size) {
                groupItemArray[i] = list[i]
            }
            var str = ""
            val gson = Gson()
            for (i in groupItemArray.indices) {
                val jsonString = gson.toJson(groupItemArray[i])
                str += jsonString
                if (i < groupItemArray.size - 1) {
                    str += "_|_"
                }
            }
            return str
        }
        return ""
    }

    @TypeConverter
    fun fromStringToIntArray(value: String): ArrayList<Int>? {
        val listType = object : TypeToken<ArrayList<Int>>() {

        }.type
        return Gson().fromJson<ArrayList<Int>>(value, listType)
    }

    @TypeConverter
    fun fromIntArrayListToString(list: ArrayList<Int>): String {
        val gson = Gson()
        return gson.toJson(list)
    }


    @TypeConverter
    fun fromStringToIntList(value: String): List<Int>? {
        val listType = object : TypeToken<List<Int>>() {

        }.type
        return Gson().fromJson<List<Int>>(value, listType)
    }

    @TypeConverter
    fun fromIntListToString(list: List<Int>): String {
        val gson = Gson()
        return gson.toJson(list)
    }


    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return if (date == null) {
            null
        } else {
            date.getTime()
        }
    }


    @TypeConverter
    fun fromBroadcastGroupMemberString(value: String): List<BroadcastGroup.Member>? {
        if (value?.isNotEmpty()) {
            val array = value.split("_|_")
            val gson = Gson()
            val list = (0 until array.size).mapTo(ArrayList()) { gson.fromJson(array[it], BroadcastGroup.Member::class.java) }
            return list
        }
        return arrayListOf()
    }

    @TypeConverter
    fun fromBroadcastGroupMemberArrayList(list: List<BroadcastGroup.Member>?): String {
        if (list != null && list.size > 0) {
            val array = arrayOfNulls<BroadcastGroup.Member>(list.size)
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

    @TypeConverter
    fun fromSendGiftResponseCategoryString(value: String): List<SendGiftResponse.Category>? {
        if (value?.isNotEmpty()) {
            val array = value.split("_|_")
            val gson = Gson()
            val list = (0 until array.size).mapTo(ArrayList()) { gson.fromJson(array[it], SendGiftResponse.Category::class.java) }
            return list
        }
        return arrayListOf()
    }

    @TypeConverter
    fun fromSendGiftResponseCategoryArrayList(list: List<SendGiftResponse.Category>?): String {
        if (list != null && list.size > 0) {
            val array = arrayOfNulls<SendGiftResponse.Category>(list.size)
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
