package com.munch.lib.android.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.SingletonHolder
import java.lang.ref.WeakReference

/**
 * Create by munch1192 on 2022/4/2 17:28.
 */
class ActivityHelper private constructor(private val app: Application) {

    companion object : SingletonHolder<ActivityHelper, Application>({ ActivityHelper(it) }) {

        fun getInstance() = getInstance(AppHelper.app)
    }

    private var currWK: WeakReference<Activity>? = null

    private val callback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            releaseCurr()
            currWK = WeakReference(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            releaseCurr()
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }

    fun register() {
        app.registerActivityLifecycleCallbacks(callback)
    }

    fun unregister() {
        releaseCurr()
        app.unregisterActivityLifecycleCallbacks(callback)
    }

    val curr: Activity?
        get() = currWK?.get()

    private fun releaseCurr() {
        currWK?.clear()
        currWK = null
    }
}