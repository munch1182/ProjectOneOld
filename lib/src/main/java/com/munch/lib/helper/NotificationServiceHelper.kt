package com.munch.lib.helper

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.AppHelper
import com.munch.lib.log.Logger

/**
 * Created by munch1182 on 2022/4/20 19:31.
 */
class NotificationServiceHelper : NotificationListenerService() {

    companion object : IARSHelper<OnNotificationChange> by ARSHelper() {
        val log = Logger("ntf", false)

        fun isEnable(context: Context = AppHelper.app) =
            NotificationManagerCompat
                .getEnabledListenerPackages(context)
                .contains(context.packageName)

        val isEnable: Boolean
            get() = isEnable(AppHelper.app)

        val request:Intent
            get() = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }


    override fun onCreate() {
        super.onCreate()
        log.log("onCreate.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log.log("onStartCommand.")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        log.log("onListenerConnected.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        log.log("onListenerDisconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        log.log("onNotificationPosted:${sbn?.packageName}.")
        notifyUpdate { it.invoke(true, sbn) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        log.log("onNotificationRemoved:${sbn?.packageName}.")
        notifyUpdate { it.invoke(false, sbn) }
    }
}
/**
 * postOrRemove post: true, remove: false
 */
typealias OnNotificationChange = (postOrRemove: Boolean, sbn: StatusBarNotification?) -> Unit