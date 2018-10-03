package com.user.ncard.util.ext

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import org.reactivestreams.Publisher

/**
 * Created by Pham on 11/9/17.
 */
fun <T> Publisher<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)

fun <T> LiveData<T>.toPublisher(lifecycleOwner: LifecycleOwner)
        = LiveDataReactiveStreams.toPublisher(lifecycleOwner, this)
