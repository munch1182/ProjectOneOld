package com.munch.project.one.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.fast.base.BaseBtnWithNoticeActivity
import com.munch.lib.fast.databinding.ItemSimpleBtnWithNoticeBinding
import com.munch.lib.helper.toDate
import com.munch.lib.notification.NotificationHelper
import com.munch.lib.notification.NotificationListenerServiceHelper
import com.munch.lib.result.with
import com.munch.project.one.R
import com.munch.project.one.notification.NotificationService

/**
 * 有的手机NotificationListenerService在app意外崩溃后再启动不会自动重新绑定
 * 解决版本：app启动后手动调用[NotificationListenerServiceHelper.requestUnbind]
 *
 * Create by munch1182 on 2021/11/17 15:41.
 */
class NotificationServiceActivity : BaseBtnWithNoticeActivity() {
    private val judge = {
        NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(packageName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setItem(
            mutableListOf(
                "request permission",
                "start service",
                "stop service",
                "enable",
                "disable",
                "new notification",
                "throw error"
            )
        )
        NotificationHelper.connectedStateChanges.set(this) { showInfo() }
        showInfo()
    }

    override fun onClick(pos: Int, bind: ItemSimpleBtnWithNoticeBinding) {
        super.onClick(pos, bind)
        when (pos) {
            0 -> {
                if (judge.invoke()) {
                    with(NotificationListenerServiceHelper.requestIntent())
                        .start { showInfo() }

                } else {
                    with(judge, NotificationListenerServiceHelper.requestIntent())
                        .start { showInfo() }
                }
            }
            1 -> {
                NotificationService.start()
            }
            2 -> {
                NotificationService.stop()
            }
            3 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NotificationService.enable()
                }
            }
            4 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NotificationService.disable()
                }
            }
            5 -> {
                val manager = NotificationManagerCompat.from(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createNotificationChannel(
                        NotificationChannel(
                            "test_notification",
                            "test_notification",
                            NotificationManager.IMPORTANCE_HIGH
                        )
                    )
                }
                manager.notify(
                    1118, NotificationCompat.Builder(this, "test_notification")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(System.currentTimeMillis().toDate())
                        .build()
                )
            }
            6 -> {
                throw RuntimeException("测试NotificationService异常")
            }
        }
    }

    private fun showInfo() {
        showNotice("permission: ${judge.invoke()}\nstate:${NotificationHelper.isConnected}")
    }
}