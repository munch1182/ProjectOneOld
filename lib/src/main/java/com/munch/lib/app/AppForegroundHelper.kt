package com.munch.lib.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.munch.lib.helper.ARSHelper

/**
 * Create by munch1182 on 2021/10/28 15:14.
 */
object AppForegroundHelper : ARSHelper<OnAppForegroundChangeListener> {

    private var activityIndex = 0
        set(value) {
            field = value
            notifyListener { it.invoke(isInForeground) }
        }
    val isInForeground: Boolean
        //有activity回调了Resumed但是没有回调Paused即处于前台
        get() = activityIndex == 1

    private val cb by lazy {
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                activityIndex++
            }

            override fun onActivityPaused(activity: Activity) {
                activityIndex--
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        }
    }

    fun register(app: Application) {
        app.registerActivityLifecycleCallbacks(cb)
    }

    fun unregister(app: Application) {
        app.unregisterActivityLifecycleCallbacks(cb)
    }

    private val changeListeners = mutableListOf<OnAppForegroundChangeListener>()

    override val arrays: MutableList<OnAppForegroundChangeListener>
        get() = changeListeners
}

typealias OnAppForegroundChangeListener = (isInForeground: Boolean) -> Unit