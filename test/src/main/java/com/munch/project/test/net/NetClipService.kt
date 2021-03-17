package com.munch.project.test.net

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.munch.lib.base.BaseForegroundService
import com.munch.lib.test.R

/**
 * Create by munch1182 on 2021/3/17 9:21.
 */
class NetClipService : BaseForegroundService(
    Parameter(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NOTIFICATION_ID)
) {

    companion object {
        private const val CHANNEL_ONE_ID = "channel_01"
        private const val CHANNEL_ONE_NAME = "channel net clip"
        private const val NOTIFICATION_ID = 1216

        fun start(context: Context) {
            startForegroundService(context, Intent(context, NetClipService::class.java))
        }

        fun stop(context: Context) {
            stop(context, Intent(context, NetClipService::class.java))
        }
    }

    override fun buildNotification(title: String?): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, parameter.channelId)
            .apply {
                if (title == null) {
                    setContentTitle("剪切板服务正在运行中")
                } else {
                    setContentTitle(title)
                }
            }
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 111,
                    Intent(this, TestNetActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            //有些手机不设置不会显示
            //有些手机默认关闭了通知
            .setSmallIcon(R.drawable.ic_small)
    }

    private fun receiverClip(content: String) {
        notify(buildNotification(content).build())
    }

}