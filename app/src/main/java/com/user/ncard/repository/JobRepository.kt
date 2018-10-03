package com.user.ncard.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Environment
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.user.ncard.AppExecutors
import com.user.ncard.SingleLiveEvent
import com.user.ncard.api.ApiResponse
import com.user.ncard.api.AppAuthenticationHandler
import com.user.ncard.api.NCardService
import com.user.ncard.db.GroupDao
import com.user.ncard.db.JobDao
import com.user.ncard.db.NcardDb
import com.user.ncard.di.network.ChangeableBaseUrlInterceptor
import com.user.ncard.util.AppHelper
import com.user.ncard.util.Constants
import com.user.ncard.util.SharedPreferenceHelper
import com.user.ncard.util.Utils
import com.user.ncard.util.ext.toLiveData
import com.user.ncard.vo.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Pham on 4/9/17.
 */
@Singleton
class JobRepository @Inject constructor(val transferUtility: TransferUtility, val groupDao: GroupDao, val jobDao: JobDao,
                                        val db: NcardDb, val changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
                                        val appExecutors: AppExecutors, val nCardService: NCardService,
                                        val sharedPreferenceHelper: SharedPreferenceHelper, val context: Context) {

    var totalFiles = 0
    var count = 0
    fun createJob(job: Job, successEvent: SingleLiveEvent<Void>,
                  mediaFilePathsList: ArrayList<String>, logoFilePathsList: ArrayList<String>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val userId = uploadFiles(job, mediaFilePathsList, logoFilePathsList)

                //if updating and no files need to be updated or it is creating new name card without any file
                if (totalFiles == 0 || totalFiles == count) {
                    createJobAfterUpload(userId, job, successEvent)
                } else {
                    val observers = transferUtility.getTransfersWithType(TransferType.UPLOAD)
                    observers
                            .asSequence()
                            .forEach {
                                if (it.state == TransferState.COMPLETED) {
                                    transferUtility.deleteTransferRecord(it.id)
                                } else {
                                    it.setTransferListener(object : TransferListener {
                                        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                                            Log.e(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                                                    id, bytesTotal, bytesCurrent))
                                        }

                                        override fun onStateChanged(id: Int, state: TransferState?) {
                                            Log.e(TAG, "onStateChanged: $id, $state")
                                            if (state == TransferState.COMPLETED) {
                                                count++
                                                transferUtility.deleteTransferRecord(id)
                                            }
                                            if (count == totalFiles) {
                                                createJobAfterUpload(userId, job, successEvent)

                                                //clear observers when done
                                                if (observers != null && !observers.isEmpty()) {
                                                    for (observerObject in observers) {
                                                        observerObject.cleanTransferListener()
                                                    }
                                                }
                                            }
                                        }

                                        override fun onError(id: Int, ex: java.lang.Exception?) {
                                            Log.e(TAG, "Error during upload: " + id, ex)
                                        }
                                    })
                                }
                            }
                }
            }
        })
    }

    fun updateJob(job: Job, successEvent: SingleLiveEvent<Void>,
                  mediaFilePathsList: ArrayList<String>, logoFilePathsList: ArrayList<String>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val userId = uploadFiles(job, mediaFilePathsList, logoFilePathsList)
                //if updating and no files need to be updated or it is creating new name card without any file
                if (totalFiles == 0 || totalFiles == count) {
                    updateJobAfterUpload(userId, job, successEvent)
                } else {
                    val observers = transferUtility.getTransfersWithType(TransferType.UPLOAD)
                    observers
                            .asSequence()
                            .forEach {
                                if (it.state == TransferState.COMPLETED) {
                                    transferUtility.deleteTransferRecord(it.id)
                                } else {
                                    it.setTransferListener(object : TransferListener {
                                        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                                            Log.e(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                                                    id, bytesTotal, bytesCurrent))
                                        }

                                        override fun onStateChanged(id: Int, state: TransferState?) {
                                            Log.e(TAG, "onStateChanged: $id, $state")
                                            if (state == TransferState.COMPLETED) {
                                                count++
                                                transferUtility.deleteTransferRecord(id)
                                            }
                                            if (count == totalFiles) {
                                                updateJobAfterUpload(userId, job, successEvent)

                                                //clear observers when done
                                                if (observers != null && !observers.isEmpty()) {
                                                    for (observerObject in observers) {
                                                        observerObject.cleanTransferListener()
                                                    }
                                                }
                                            }
                                        }

                                        override fun onError(id: Int, ex: java.lang.Exception?) {
                                            Log.e(TAG, "Error during upload: " + id, ex)
                                        }
                                    })
                                }
                            }
                }
            }
        })
    }

    private fun createJobAfterUpload(userId: Int, job: Job, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                nCardService.createJobForUser(userId, job)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "create job success")
                                    successEvent.call()
                                    compositeDisposable.add(CompletableFromAction(Action { jobDao.insert(job) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success insert job table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to insert into job table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to create", error) })
            }
        })
    }

    private fun updateJobAfterUpload(userId: Int, job: Job, createNameCardSuccess: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                val compositeDisposable = CompositeDisposable()
                nCardService.updateJobForUser(userId, job.id, job)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "update job success")
                                    createNameCardSuccess.call()
                                    compositeDisposable.add(CompletableFromAction(Action { jobDao.updateJob(job) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success update job table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to update job table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to update", error) })
            }
        })
    }

    private fun uploadFiles(job: Job, mediaFilePathsList: ArrayList<String>, logoFilePathsList: ArrayList<String>): Int {
        val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
        changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
        count = 0
        totalFiles = 0

        if (mediaFilePathsList.isNotEmpty()) {
            totalFiles += mediaFilePathsList.size
            job.media = ArrayList()
            mediaFilePathsList.forEach {
                if (it.contains("http")) {
                    job.media?.add(it)
                    count++
                } else {
                    val file = File(it)
                    val key = "job/"
                    transferUtility.upload(AppHelper.getCognitoBucketName(), key + file.name, file)
                    job.media?.add(Utils.getFilePathInS3NameCard(key, file.name))
                }
            }
        } else {
            job.media = ArrayList()
        }
        if (logoFilePathsList.isNotEmpty()) {
            totalFiles += logoFilePathsList.size
            job.cert = ArrayList()
            logoFilePathsList.forEach {
                if (it.contains("http")) {
                    job.cert?.add(it)
                    count++
                } else {
                    val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + "drawable" + UUID.randomUUID().toString() + ".png")
                    val inputStream = context.resources.openRawResource(it.toInt())
                    val out = FileOutputStream(file)
                    val buf = ByteArray(1024)
                    var len: Int
                    len = inputStream.read(buf)
                    while (len > 0) {
                        out.write(buf, 0, len)
                        len = inputStream.read(buf)
                    }
                    out.close()
                    inputStream.close()
                    val key = "job/"
                    transferUtility.upload(AppHelper.getCognitoBucketName(), key + file.name, file)
                    job.cert?.add(Utils.getFilePathInS3NameCard(key, file.name))
                }
            }
        } else {
            job.cert = ArrayList()
        }
        return userId
    }

    fun updateGroup(group: Group, updateSuccess: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val memberIds = ArrayList<Int>()
                val members = if (group.members?.isEmpty() != false) memberIds else {
                    group.members?.forEach {
                        if (it != null) memberIds.add(it.id)
                    }
                    memberIds
                }
                val nameCardIds = ArrayList<Int>()
                val nameCards = if (group.nameCards?.isEmpty() != false) nameCardIds else {
                    group.nameCards?.forEach {
                        if (it != null) nameCardIds.add(it.id)
                    }
                    nameCardIds
                }
                val userId = sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID)
                val compositeDisposable = CompositeDisposable()
                nCardService.updateGroupForUser(userId, group.id, RequestGroup(group.name, members, nameCards))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "update group success")
                                    updateSuccess.call()
                                    compositeDisposable.add(CompletableFromAction(Action { groupDao.insert(group) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success group card table")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to update group table", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to update", error) })
            }
        })
    }

    fun getALlJobs(): LiveData<Resource<List<Job>>> {
        changeableBaseUrlInterceptor.setInterceptor(Constants.USER_INFO_SERVICE_URL)
        return object : NetworkBoundResource<List<Job>, List<Job>>(appExecutors, sharedPreferenceHelper) {
            override fun saveCallResult(item: List<Job>) {
                db.beginTransaction()
                try {
                    jobDao.deleteAllJobs()
                    jobDao.insertJobs(item)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            override fun shouldFetch(data: List<Job>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Job>> {

                return jobDao.findAllJobs()
            }

            override fun createCall(): LiveData<ApiResponse<List<Job>>> {
                return nCardService.getAllJobs(sharedPreferenceHelper.getInt(SharedPreferenceHelper.Key.CURRENT_USER_ID))
            }
        }.asLiveData()
    }

    fun getJobsForUser(userId: Int): LiveData<List<Job>> {
        var result: MutableLiveData<List<Job>> = MediatorLiveData<List<Job>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getAllJobsForUser(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                data.subscribe({ it ->
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun getNameCardForUser(userId: Int): LiveData<List<NameCard>> {
        var result: MutableLiveData<List<NameCard>> = MediatorLiveData<List<NameCard>>()
        Utils.getToken(sharedPreferenceHelper, object : AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_REQUEST_SERVICE_URL)
                val data = nCardService.getAllNameCardFromUser(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                data.subscribe({ it ->
                    result.value = it
                }, { error ->
                    Log.e(TAG, "Unable to get response" + (error as HttpException).response().errorBody()?.string())
                })
            }
        })
        return result
    }

    fun deleteJob(userId: Int, jobId: Int, successEvent: SingleLiveEvent<Void>) {
        Utils.getToken(sharedPreferenceHelper, object: AppAuthenticationHandler {
            override fun onSuccessAuthentication(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                changeableBaseUrlInterceptor.setInterceptor(Constants.FRIEND_INFO_SERVICE_URL)
                val compositeDisposable = CompositeDisposable()
                nCardService.deleteJob(userId, jobId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    Log.e(TAG, "delete job success")
                                    successEvent.call()
                                    compositeDisposable.add(CompletableFromAction(Action { jobDao.deleteJobById(jobId) })
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                Log.e("NCard", "Success delete job from db")
                                                compositeDisposable.dispose()
                                            }, { error ->
                                                Log.e("NCard", "Unable to delete job from db", error)
                                                compositeDisposable.dispose()
                                            }))
                                },
                                { error -> Log.e(TAG, "Unable to send", error) })
            }
        })
    }

    companion object {
        private val TAG = "NCard"
    }
}