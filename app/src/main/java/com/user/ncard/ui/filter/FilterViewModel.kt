package com.user.ncard.ui.filter

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.user.ncard.repository.FilterRepository
import com.user.ncard.vo.FilterObject
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class FilterViewModel @Inject constructor(val filterRepository: FilterRepository) : ViewModel() {
    val nationalities: MutableLiveData<ArrayList<FilterObject>> = MutableLiveData()
    val countries: MutableLiveData<ArrayList<FilterObject>> = MutableLiveData()
    val industries: MutableLiveData<ArrayList<FilterObject>> = MutableLiveData()

    fun getFilterData() {
        industries.value = ArrayList()
        countries.value = ArrayList()
        nationalities.value = ArrayList()
        filterRepository.getFilterData(industries, nationalities, countries)
    }
}