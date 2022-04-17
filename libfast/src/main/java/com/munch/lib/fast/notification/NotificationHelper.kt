package com.munch.lib.fast.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.AppHelper
import com.munch.lib.extend.SingletonHolder
import com.munch.lib.fast.R

/**
 * Create by munch1182 on 2022/4/16 15:01.
 */
class NotificationHelper(private val context: Context) {

    companion object : SingletonHolder<NotificationHelper, Context>({ NotificationHelper(it) }) {

        fun getInstance() = getInstance(AppHelper.app)

        private const val CHANNEL_ID_DEF = "notification"
        private const val CHANNEL_NAME_DEF = "notification"
        private const val NOTIFY_ID_DEF = 416
    }

    val isEnable: Boolean
        get() = nm.areNotificationsEnabled()

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestIntent(): Intent {
        return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    }

    fun notify(msg: String, id: Int = NOTIFY_ID_DEF) {
        nm.notify(id, buildNotification(msg).build())
    }

    fun cancel(id: Int = NOTIFY_ID_DEF) {
        nm.cancel(id)
    }

    private val nm: NotificationManagerCompat
        get() = NotificationManagerCompat.from(context)

    private fun buildNotification(content: String): NotificationCompat.Builder {

        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(
                CHANNEL_ID_DEF,
                NotificationManagerCompat.IMPORTANCE_HIGH
            ).setName(CHANNEL_NAME_DEF).build()
        )
        return NotificationCompat.Builder(context, CHANNEL_ID_DEF)
            .setContentText(content)
            //必须
            .setSmallIcon(R.drawable.ic_android_24dp)
    }

}