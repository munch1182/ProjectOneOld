package com.munch.lib.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.munch.lib.helper.AppStatusHelper.getForegroundLiveData
import com.munch.lib.helper.AppStatusHelper.isForeground
import com.munch.lib.helper.AppStatusHelper.register
import java.util.*

/**
 * 1. 获取app当前前台activity对象
 * 2. 通过activity计数的方式来判断activity是否在前台
 * 需要先在Application中[register]
 * 然后通过[isForeground]或者[getForegroundLiveData]观察是否在前台
 *
 * 前台应用锁屏后也是后台，解锁后才回到前台
 *
 * Create by munch1182 on 2020/12/17 13:46.
 */
object AppStatusHelper {

    private var activityCount = 0
        set(value) {
            if ((field > 0 && value == 0) || (field == 0 && value > 0)) {
                countLiveData.postValue(value > 0)
            }
            field = value
        }

    private val countLiveData by lazy {
        MutableLiveData<Boolean>().apply {
            value = isForeground()
        }
    }

    private val stack = Stack<Activity>()

    fun isForeground() = activityCount > 0

    /**
     * 获取本app当前显示的activity
     *
     * 注意：获取到的activity生命周期在start-pause之间，超出此生命周期的activity无法从此处获得
     * 注意：需要在每一处使用时都判断是否为null，因为activity随时可能被关闭
     * 注意：注意内存泄漏的问题
     */
    fun getTopActivity(): Activity? = if (stack.isEmpty()) null else stack.peek()

    /**
     * @see MutableLiveData.observeForever
     */
    fun getForegroundLiveData(): LiveData<Boolean> {
        return countLiveData
    }

    fun register(app: Application): AppStatusHelper {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {
                stack.pop()
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
                stack.push(activity)
            }
        })
        return this
    }
}