package com.user.ncard

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.support.v7.app.AppCompatActivity

/**
 * Created by Pham on 4/9/17.
 */
/**
 * Temporary class until Architecture Components is final. Makes [AppCompatActivity] a
 * [LifecycleRegistryOwner].
 */
open class LifecycleAppCompatActivity : AppCompatActivity(), LifecycleRegistryOwner {

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry = registry
}