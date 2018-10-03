package com.user.ncard.api

import android.arch.lifecycle.LiveData
import com.user.ncard.ui.catalogue.*
import com.user.ncard.vo.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Pham on 27/7/17.
 */

interface ScanCardService {
    @Multipart
    @POST("BCRService/BCR_VCF2?PIN=&user=yuanfu@user.com.sg&pass=PE8WFQCMEGMGG8AN&json=1&lang=15")
    fun scanNameCard(@Query("size") size: Long, @Part file: MultipartBody.Part): Call<CamCardApiResponse>
}