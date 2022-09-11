package com.munch.lib.android.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.toOrNull
import java.lang.ref.WeakReference

/**
 * 收集并提供当前Activity对象
 *
 * 是一个LiveData对象, 会在Activity进入Resume时更新为当前Activity并在Pause时更新值为null
 *
 * 对于需要当前Activity对象的方法, 当当前Activity为null时, 可在下一次更新Activity时再执行
 */
class ActivityStackHelper<ACT : Activity> : LiveData<ACT?>() {

    private var actRef: WeakReference<ACT?>? = null
        set(value) {
            field = value
            postValue(value?.get())
        }

    override fun onActive() {
        super.onActive()
        AppHelper.toApp().registerActivityLifecycleCallbacks(callback)
    }

    override fun onInactive() {
        super.onInactive()
        AppHelper.toApp().unregisterActivityLifecycleCallbacks(callback)
        clear()
    }

    private val callback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            add(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            clear()
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }

    private fun add(act: Activity?) {
        act?.toOrNull<ACT>()?.let { actRef = WeakReference(it) }
    }

    private fun clear() {
        actRef?.clear()
        actRef = null
    }
}