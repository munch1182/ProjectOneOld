package com.munch.lib.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.munch.lib.AppHelper
import com.munch.lib.extend.toLive
import java.lang.ref.WeakReference

/**
 * Create by munch1192 on 2022/4/2 17:28.
 */
object ActivityHelper {

    //<editor-fold desc="imp">
    private var currWK: WeakReference<Activity>? = null
    private var currCreateWK: WeakReference<Activity>? = null
    private var refCount = 0
        set(value) {
            if (field == value) {
                return
            }
            val count = field
            field = value
            if ((value == 0 && count != 0) || (value != 0 && count == 0)) {
                isForegroundLive.postValue(isForeground)
            }
        }
    private val isForegroundLive = MutableLiveData(isForeground)

    private val callback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            currCreateWK = WeakReference(activity)
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            refCount++
            releaseCurr()
            currWK = WeakReference(activity)
            resumeCheck()
        }

        override fun onActivityPaused(activity: Activity) {
            refCount--
            releaseCurr()
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            releaseCreate()
        }
    }

    private fun releaseCurr() {
        currWK?.clear()
        currWK = null
    }

    private fun releaseCreate() {
        currCreateWK?.clear()
        currCreateWK = null
    }

    private fun resumeCheck() {
    }
    //</editor-fold>

    fun register(app: Application = AppHelper.app) {
        app.registerActivityLifecycleCallbacks(callback)
    }

    fun unregister(app: Application = AppHelper.app) {
        releaseCurr()
        app.unregisterActivityLifecycleCallbacks(callback)
    }

    val curr: Activity?
        get() = currWK?.get()
    val currCreate:Activity?
        get() = currCreateWK?.get()

    val isForeground: Boolean
        get() = refCount >= 1

    val isForegroundLiveData = isForegroundLive.toLive()
}