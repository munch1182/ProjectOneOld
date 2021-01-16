package com.munch.lib.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.munch.lib.helper.AppForegroundStatusHelper.getForegroundLiveData
import com.munch.lib.helper.AppForegroundStatusHelper.isForeground
import com.munch.lib.helper.AppForegroundStatusHelper.register

/**
 * 通过activity计数的方式来判断activity是否在前台
 * 需要现在在Application中[register]
 * 然后通过[isForeground]或者[getForegroundLiveData]观察是否在前台
 *
 * 前台应用锁屏后也是后台，解锁后才回到前台
 *
 * Create by munch1182 on 2020/12/17 13:46.
 */
object AppForegroundStatusHelper {

    private var activityCount = 0
        set(value) {
            if ((field > 0 && value == 0) || (field == 0 && value > 0)) {
                getForegroundLiveData().postValue(value > 0)
            }
            field = value
        }

    private val countLiveData by lazy {
        MutableLiveData<Boolean>().apply {
            value = isForeground()
        }
    }

    fun isForeground() = activityCount > 0

    /**
     * 使用[MutableLiveData.observeForever]
     */
    fun getForegroundLiveData(): MutableLiveData<Boolean> {
        return countLiveData
    }

    fun register(app: Application): AppForegroundStatusHelper {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStarted(activity: Activity) {
                activityCount++
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
                activityCount--
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityResumed(activity: Activity) {
            }
        })
        return this
    }
}