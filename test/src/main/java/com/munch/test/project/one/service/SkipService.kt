package com.munch.test.project.one.service

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.pre.lib.helper.service.IForegroundService
import com.munch.pre.lib.log.log
import com.munch.test.project.one.R

/**
 * Create by munch1182 on 2021/5/17 11:40.
 */
class SkipService : AccessibilityService(), IForegroundService {

    companion object {

        fun stop(context: Context, intent: Intent) {
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        log("skip service onCreate")
        startForeground()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        log("skip service onServiceConnected")
        notify(buildNotification("skip service is running").build())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            when (event.packageName) {
                "com.netease.cloudmusic" -> {
                }
                "tv.danmaku.bili" -> {
                }
            }

        }
    }

    override fun onInterrupt() {
        notify(buildNotification("skip service stop").build())
        stopSelf()
    }

    override fun buildNotification(title: String?): NotificationCompat.Builder {
        return super.buildNotification(title ?: "skip service start")
            .setSmallIcon(R.mipmap.ic_launcher)
    }

    override val service: Service by lazy { this }
    override val parameter = IForegroundService.Parameter("skip", "skip service", 9527)
    override val manager by lazy { NotificationManagerCompat.from(this) }
}