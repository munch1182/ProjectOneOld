package com.munch.lib.base

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.RequiresPermission
import com.munch.lib.helper.startServiceInForeground

/**
 * 用于处理前台服务的通用逻辑
 * Create by munch1182 on 2020/12/16 11:48.
 */
@RequiresPermission("android.permission.FOREGROUND_SERVICE")
open class BaseForegroundService(val parameter: Parameter) : Service() {

    companion object {

        fun startForegroundService(context: Context, intent: Intent) {
            context.startServiceInForeground(intent)
        }

        fun stop(context: Context, intent: Intent) {
            context.stopService(intent)
        }
    }

    val manager by lazy { NotificationManagerCompat.from(this) }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    parameter.channelId,
                    parameter.channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        startForeground(parameter.serviceId, buildNotification().build())
    }

    open fun buildNotification(title: String? = null): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, parameter.channelId).apply {
            if (title != null) {
                setContentTitle(title)
            }
        }
    }

    fun cancel() {
        manager.cancel(parameter.serviceId)
    }

    fun notify(notification: Notification = buildNotification().build()) {
        manager.notify(parameter.serviceId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    class Parameter(channelId: String, val channelName: String, val serviceId: Int) {

        //低版本的channelId需为空
        val channelId: String = channelId
            get() {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    field
                } else {
                    ""
                }
            }
    }
}