package com.munch.project.one.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.app.AppHelper
import com.munch.lib.helper.service.IForegroundService
import com.munch.lib.helper.toDate
import com.munch.lib.notification.NotificationListenerServiceHelper
import com.munch.project.one.R

/**
 * Create by munch1182 on 2021/10/22 10:04.
 */
class NotificationService : NotificationListenerServiceHelper(), IForegroundService {

    companion object {
        @RequiresApi(Build.VERSION_CODES.N)
        fun enable() = enable(cls = NotificationService::class.java)

        @RequiresApi(Build.VERSION_CODES.N)
        fun disable() = disable(cls = NotificationService::class.java)

        fun start(context: Context = AppHelper.app) {
            context.startService(Intent(context, NotificationService::class.java))
        }

        fun stop(context: Context = AppHelper.app) {
            context.stopService(Intent(context, NotificationService::class.java))
        }

        private const val SERVICE_ID = 1203
        private const val CHANNEL_ID = "NotificationListenerService"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        startForeground()
    }

    override fun buildNotification(title: String?): NotificationCompat.Builder {
        return super.buildNotification(getString(R.string.app_name))
            .setContentText(
                "NotificationListenerService is Running, start at " +
                        "${System.currentTimeMillis().toDate()}."
            ).setSmallIcon(R.mipmap.ic_launcher)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        onRequestUnbind()
    }

    override fun onRequestUnbind() {
        super.onRequestUnbind()
        notify(
            buildNotification()
                .setContentText(
                    "NotificationListenerService had bean stopped, at " +
                            "${System.currentTimeMillis().toDate()}."
                ).build()
        )
    }

    override val service: Service
        get() = this
    override val parameter: IForegroundService.Parameter
        get() = IForegroundService.Parameter(CHANNEL_ID, CHANNEL_ID, SERVICE_ID)
    override val manager: NotificationManagerCompat
        get() = NotificationManagerCompat.from(this)
}