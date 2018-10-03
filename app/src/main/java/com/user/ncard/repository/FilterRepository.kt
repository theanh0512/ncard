package com.user.ncard.repository

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.user.ncard.db.FilterDao
import com.user.ncard.vo.FilterObject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class FilterRepository @Inject constructor(val filterDao: FilterDao) {
    fun getFilterData(industries: MutableLiveData<ArrayList<FilterObject>>, nationalities: MutableLiveData<ArrayList<FilterObject>>, countries: MutableLiveData<ArrayList<FilterObject>>) {
        launch(CommonPool + CoroutineExceptionHandler({ _, e ->
            Log.e("NCard", "CoroutineExceptionHandler" + e.toString(), e)
        })) {
            val industryList = async(CommonPool) { filterDao.getAllIndustryFilters() }.await()?.map { filter -> FilterObject(filter.name, false) }
            industries.value!!.addAll(industryList ?: ArrayList())
            val nationalityList = async(CommonPool) { filterDao.getAllNationalityFilters() }.await()?.map { filter -> FilterObject(filter.name, false) }
            nationalities.value!!.addAll(nationalityList ?: ArrayList())
            val countryList = async(CommonPool) { filterDao.getAllCountryFilters() }.await()?.map { filter -> FilterObject(filter.name, false) }
            countries.value!!.addAll(countryList ?: ArrayList())
        }
    }

    companion object {
        private val TAG = "NCard"
    }
}