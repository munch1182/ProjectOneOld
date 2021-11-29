package com.munch.lib.notification

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.app.AppHelper
import com.munch.lib.helper.SimpleARSHelper

/**
 *
 * @see NotificationListenerServiceHelper
 *
 * Create by munch1182 on 2021/10/23 10:20.
 */
object NotificationHelper {

    val notificationChanges = SimpleARSHelper<OnNotificationChange>()
    val connectedStateChanges = SimpleARSHelper<OnConnectedStateChange>()
    private var isNotificationConnected = false
    val isConnected: Boolean
        get() = isNotificationConnected

    //<editor-fold desc="update">
    internal fun onConnectedStateChange(isConnected: Boolean) {
        isNotificationConnected = isConnected
        connectedStateChanges.notifyListener { it.invoke(isConnected) }
    }

    internal fun onRemoved(sbn: StatusBarNotification) {
        notificationChanges.notifyListener { it.invoke(sbn, false) }
    }

    internal fun onPosted(sbn: StatusBarNotification) {
        notificationChanges.notifyListener { it.invoke(sbn, true) }
    }
    //</editor-fold>

    //<editor-fold desc="notification">
    private val nm: NotificationManagerCompat
        get() = NotificationManagerCompat.from(AppHelper.app)

    /**
     * 应用是否能够发送通知
     */
    val canNotify: Boolean
        get() = NotificationManagerCompat.from(AppHelper.app).areNotificationsEnabled()

    /**
     * 发出一个通知，显示到通知栏
     *
     * 此通知会默认使用IMPORTANCE_HIGH，因为有的手机会在显示后立即移除其它通知
     *
     * @param channelId 通知的渠道ID，如果app会分不同的渠道，则应该自定义类来归类并发送通知
     */
    fun notification(
        notificationId: Int,
        channelId: String,
        notification: Notification,
        channelName: String = channelId
    ) {
        val n = nm
        n.getNotificationChannel(channelId) ?: n.createNotificationChannel(
            NotificationChannelCompat.Builder(
                channelId, NotificationManagerCompat.IMPORTANCE_HIGH
            ).setName(channelName).build()
        )
        n.notify(notificationId, notification)
    }

    fun cancel(notificationId: Int) {
        nm.cancel(notificationId)
    }
    //</editor-fold>
}

typealias OnConnectedStateChange = (isConnected: Boolean) -> Unit
typealias OnNotificationChange = (sbn: StatusBarNotification, isPosted: Boolean) -> Unit