package com.munch.lib.test.def

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.munch.lib.base.BaseForegroundService
import com.munch.lib.test.R

/**
 * Create by munch1182 on 2020/12/16 11:23.
 */
open class DefForegroundService : BaseForegroundService(
    Parameter(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NOTIFICATION_ID)
) {

    companion object {
        private const val CHANNEL_ONE_ID = "channel def"
        private const val CHANNEL_ONE_NAME = "channel name"
        private const val NOTIFICATION_ID = 1216
    }

    override fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, parameter.channelId).setContentTitle("test service")
            //有些手机不设置不会显示
            //有些手机默认关闭了通知
            .setSmallIcon(R.drawable.ic_small).build()
    }

}