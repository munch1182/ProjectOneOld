package com.munch.lib.notification

import android.service.notification.StatusBarNotification
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
}

typealias OnConnectedStateChange = (isConnected: Boolean) -> Unit
typealias OnNotificationChange = (sbn: StatusBarNotification, isPosted: Boolean) -> Unit