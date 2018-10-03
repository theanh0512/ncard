package com.user.ncard.ui.me

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.user.ncard.repository.JobRepository
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Group
import com.user.ncard.vo.Job
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.Resource
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class MyNameCardViewModel @Inject constructor(val nameCardRepository: NameCardRepository,
                                              val jobRepository: JobRepository,
                                              val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val start = MutableLiveData<Boolean>()
    val nameCardList: LiveData<Resource<List<NameCard>>>
    val jobList: LiveData<Resource<List<Job>>>

    init {
        nameCardList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<NameCard>>>()
            } else {
                return@switchMap nameCardRepository.getAllMyNameCards()
            }
        }
        jobList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<Job>>>()
            } else {
                return@switchMap jobRepository.getALlJobs()
            }
        }
    }
}
