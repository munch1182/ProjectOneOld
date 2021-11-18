package com.munch.project.one.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.munch.lib.app.AppHelper
import com.munch.lib.notification.NotificationListenerServiceHelper

/**
 * Create by munch1182 on 2021/10/22 10:04.
 */
class NotificationService : NotificationListenerServiceHelper() {

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
    }
}