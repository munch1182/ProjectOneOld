package com.munch.project.testsimple.alive

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import androidx.annotation.RequiresPermission

/**
 * Create by munch1182 on 2020/12/14 10:49.
 */
object AliveHelper {

    /**
     * 虽然方法已废弃，但仍会返回自己的服务
     */
    @Suppress("DEPRECATION")
    fun isServiceRunning(context: Context, service: Class<out Service>): Boolean {
        val manager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
        val services = manager.getRunningServices(Int.MAX_VALUE)
        services.forEach {
            if (it.service.className == service.name) {
                return true
            }
        }
        return false
    }

    fun isAppRunningForeground(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager? ?: return false
        val processes = manager.runningAppProcesses ?: return false
        processes.forEach {
            if (it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && it.processName == context.applicationInfo.processName
            ) {
                return true
            }
        }
        return false
    }

    /**
     * 将后台应用移动到前台
     *
     * 测试未成功(android10)
     */
    @Suppress("DEPRECATION")
    @RequiresPermission("android.permission.REORDER_TASKS")
    fun moveApp2Front(context: Context) {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.getRunningTasks(30).forEach {
            if (it.topActivity!!.packageName == context.packageName) {
                manager.moveTaskToFront(it.id, 0)
                return
            }
        }
    }
}