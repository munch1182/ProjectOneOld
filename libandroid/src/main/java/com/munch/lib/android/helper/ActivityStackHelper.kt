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
open class ActivityStackHelper<ACT : Activity> : LiveData<ACT?>() {

    private var actRef: WeakReference<ACT?>? = null
        set(value) {
            field = value
            postValue(value?.get())
        }
    protected open var isRegister = false

    override fun onActive() {
        super.onActive()
        register()
    }

    override fun onInactive() {
        super.onInactive()
        unregister()
    }

    fun register() {
        if (isRegister) return
        isRegister = true
        AppHelper.to().registerActivityLifecycleCallbacks(callback)
    }

    fun unregister() {
        if (!isRegister) return
        AppHelper.to().unregisterActivityLifecycleCallbacks(callback)
        clear()
        isRegister = false
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

    protected open fun add(act: Activity?) {
        act?.toOrNull<ACT>()?.let { actRef = WeakReference(it) }
    }

    protected open fun clear() {
        actRef?.clear()
        actRef = null
    }
}