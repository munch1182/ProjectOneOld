package com.munch.project.one.notification

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.munch.lib.log.log
import com.munch.lib.notification.NotificationServiceHelper

/**
 * Create by munch1182 on 2021/10/22 10:04.
 */
class NotificationService : NotificationServiceHelper() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return
        log("post: tag:${sbn.tag},${sbn.packageName},${sbn.notification.extras},${sbn.postTime}}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn ?: return
        val extras = sbn.notification.extras
        log("Removed: tag:${sbn.tag},${sbn.packageName},$extras,${sbn.postTime}}")
        log("extras:${extras.getString(Notification.EXTRA_TITLE)},${extras.getString(Notification.EXTRA_TEXT)}")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationRemoved(sbn, rankingMap)
    }
}