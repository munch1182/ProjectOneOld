package com.munch.test.service

import android.app.Activity
import android.app.ActivityManager
import android.content.Context

/**
 * Create by munch on 2020/11/30 20:00
 */
object ServiceHelper {


    /**
     * 在android26,即{@link android.os.Build.VERSION_CODES#O}时已被废弃
     * 但仍然可以返回自己的服务
     * 还是可以用来判断服务是否运行
     */
    @Suppress("DEPRECATION")
    fun getService(context: Context): MutableList<ActivityManager.RunningServiceInfo>? {
        val manager = context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager?
        manager ?: return null
        return manager.getRunningServices(Int.MAX_VALUE)
    }
}