package com.munch.pre.lib.helper.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.pre.lib.extend.startServiceInForeground

/**
 * Create by munch1182 on 2021/4/25 16:03.
 */
interface IForegroundService {

    companion object {

        @RequiresPermission("android.permission.FOREGROUND_SERVICE")
        fun startForegroundService(context: Context, intent: Intent) {
            context.startServiceInForeground(intent)
        }

        fun stop(context: Context, intent: Intent) {
            context.stopService(intent)
        }
    }

    val service: Service
    val parameter: Parameter
    val manager: NotificationManagerCompat

    fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    parameter.channelId,
                    parameter.channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        service.startForeground(parameter.serviceId, buildNotification().build())
    }

    fun buildNotification(title: String? = null): NotificationCompat.Builder {
        return NotificationCompat.Builder(service, parameter.channelId).apply {
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