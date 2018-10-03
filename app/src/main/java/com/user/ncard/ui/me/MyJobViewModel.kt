package com.user.ncard.ui.me

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableParcelable
import android.util.Log
import com.user.ncard.SingleLiveEvent
import com.user.ncard.repository.JobRepository
import com.user.ncard.repository.NameCardRepository
import com.user.ncard.util.AbsentLiveData
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.vo.Job
import com.user.ncard.vo.NameCard
import com.user.ncard.vo.Resource
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by Pham on 4/9/17.
 */
class MyJobViewModel @Inject constructor(val nameCardRepository: NameCardRepository, val jobRepository: JobRepository,
                                         val sharedPreferenceHelper: SharedPreferenceHelper) : ViewModel() {
    val start = MutableLiveData<Boolean>()
    val jobList: LiveData<Resource<List<Job>>>
    val job = ObservableParcelable<Job>()
    val successEvent = SingleLiveEvent<Void>()
    val deleteSuccessEvent = SingleLiveEvent<Void>()
    val currentlyWorkHere: ObservableBoolean = ObservableBoolean(false)
    val nameCardList: LiveData<Resource<List<NameCard>>>

    init {
        jobList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<Job>>>()
            } else {
                return@switchMap jobRepository.getALlJobs()
            }
        }
        nameCardList = Transformations.switchMap(start) { start ->
            if (start == null) {
                return@switchMap AbsentLiveData.create<Resource<List<NameCard>>>()
            } else {
                return@switchMap nameCardRepository.getAllMyNameCards()
            }
        }
    }

    fun setCurrentlyWorkHere(isWorkingHere: Boolean) {
        currentlyWorkHere.set(isWorkingHere)
    }

    fun deleteJob() {
        if (job.get() != null)
            jobRepository.deleteJob(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID), job.get()!!.id, deleteSuccessEvent)
    }

    fun createJob(mediaFilePathsList: ArrayList<String>, logoFilePathsList: ArrayList<String>, start: String, end: String) {
        if (job.get() != null) {
            formatDate(start, true)
            formatDate(end, false)
            if (currentlyWorkHere.get()) job.get()?.to = ""
            jobRepository.createJob(job.get()!!, successEvent, mediaFilePathsList, logoFilePathsList)
        }
    }

    fun updateJob(mediaFilePathsList: ArrayList<String>, logoFilePathsList: ArrayList<String>, start: String, end: String) {
        if (job.get() != null) {
            formatDate(start, true)
            formatDate(end, false)
            if (currentlyWorkHere.get()) job.get()?.to = ""
            jobRepository.updateJob(job.get()!!, successEvent, mediaFilePathsList, logoFilePathsList)
        }
    }

    private fun formatDate(dateString: String?, isStart: Boolean) {
        if (!dateString.isNullOrEmpty()) {
            val currentFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val uploadFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            try {
                val d = currentFormat.parse(dateString)
                if (isStart) job.get()!!.from = uploadFormat.format(d)
                else job.get()!!.to = uploadFormat.format(d)
            } catch (ex: ParseException) {
                Log.e("NCard", "Unable to parse date")
            }
        }
    }
}
