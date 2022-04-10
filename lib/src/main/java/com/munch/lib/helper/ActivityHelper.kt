package com.munch.lib.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Create by munch1192 on 2022/4/2 17:28.
 */
object ActivityHelper {

    private var currWK: WeakReference<Activity>? = null

    private val callback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            releaseCurr()
            currWK = WeakReference(activity)
            resumeCheck()
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

    fun register(app: Application) {
        app.registerActivityLifecycleCallbacks(callback)
    }

    fun unregister(app: Application) {
        releaseCurr()
        app.unregisterActivityLifecycleCallbacks(callback)
    }

    val curr: Activity?
        get() = currWK?.get()

    private fun releaseCurr() {
        currWK?.clear()
        currWK = null
    }

    private fun resumeCheck() {
    }
}