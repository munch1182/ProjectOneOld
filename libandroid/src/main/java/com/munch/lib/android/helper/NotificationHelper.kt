package com.munch.lib.android.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.*

/**
 * 通过[Context.bindService]的方法创建前台服务
 *
 * 需要自建返回一个[Service.onBind]返回[IServiceBinder]的Service:
 * class NotifyService : Service() {
 *      override fun onBind(p0: Intent?): IBinder {
 *          return ServiceBinder()
 *      }
 *      inner class ServiceBinder : Binder(), NotificationHelper.IServiceBinder {
 *          override fun getService(): Service {
 *              return this@NotifyService
 *          }
 *      }
 * }
 * 且该service需要注册
 *
 * Create by munch1182 on 2022/10/20 16:25.
 */
class NotificationHelper(
    name: String,
    private val serviceId: Int,
    private val serviceType: Int = ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
) {

    private var _channel: NotificationChannel? = null

    private val nm by lazy {
        AppHelper.getSystemService(Context.NOTIFICATION_SERVICE).to<NotificationManager>()
    }

    init {
        _channel = createChannel(name)
    }

    val conn = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            if (p1 is IServiceBinder) {
                service = p1.getService()
                onBind()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
        }

    }

    private var service: Service? = null

    fun isBind() = getInMain { service != null }

    @RequiresPermission(android.Manifest.permission.FOREGROUND_SERVICE)
    inline fun <reified SERVICE : Service> start() {
        impInMain {
            if (isBind()) return@impInMain
            AppHelper.bindService(
                Intent(AppHelper, SERVICE::class.java),
                conn,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun onBind() {
        service?.startForeground(
            serviceId,
            NotificationCompat.Builder(AppHelper, _channel!!.id).build(),
            serviceType
        )
    }

    fun stop() {
        impInMain {
            if (!isBind()) return@impInMain
            service = null
            conn.let { AppHelper.unbindService(it) }
        }
    }

    fun notify(update: NotificationCompat.Builder.() -> Unit) {
        nm.notify(
            serviceId,
            NotificationCompat.Builder(AppHelper, _channel!!.id).apply(update).build()
        )
    }

    private fun createChannel(name: String) = NotificationChannel(
        name, name, NotificationManager.IMPORTANCE_HIGH
    ).apply { nm.createNotificationChannel(this) }

    interface IServiceBinder {

        fun getService(): Service
    }
}