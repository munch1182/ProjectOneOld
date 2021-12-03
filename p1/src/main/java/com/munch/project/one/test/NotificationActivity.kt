package com.munch.project.one.test

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.helper.service.BaseForegroundService
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
class NotificationActivity : BaseBtnFlowActivity() {
    private val judge = {
        NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(packageName)
    }

    override fun getData() = mutableListOf(
        "request permission",
        "start notification service",
        "stop notification service",
        "enable",
        "disable",
        "enable2",
        "disable2",
        "new notification",
        "start foreground service",
        "stop foreground service",
        "throw error"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.connectedStateChanges.set(this) { showInfo() }
        flowLayout.set { group = mutableListOf(1,2,2,2,1,2,1).toTypedArray() }
        showInfo()
    }

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            //request permission
            0 -> {
                if (judge.invoke()) {
                    with(NotificationListenerServiceHelper.requestIntent())
                        .start { showInfo() }

                } else {
                    with(judge, NotificationListenerServiceHelper.requestIntent())
                        .start { showInfo() }
                }
            }
            //start notification service
            1 -> {
                NotificationService.start()
            }
            //stop notification service
            2 -> {
                NotificationService.stop()
            }
            //enable
            3 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NotificationService.enable()
                }
            }
            //disable
            4 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NotificationService.disable()
                }
            }
            //enable2
            5 -> {
                val pm = packageManager
                pm.setComponentEnabledSetting(
                    ComponentName(this, NotificationService::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    ComponentName(this, NotificationService::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
                )
            }
            //disable2
            6 -> {
                val pm = packageManager
                pm.setComponentEnabledSetting(
                    ComponentName(this, NotificationService::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
                )
            }
            //new notification
            7 -> {
                val channelId = BaseForegroundService.DEF_CHANNEL_ID
                NotificationHelper.notification(
                    BaseForegroundService.DEF_SERVICE_ID, channelId,
                    NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(System.currentTimeMillis().toDate())
                        .build()
                )
            }
            //start foreground service
            8 -> {
                BaseForegroundService.start()
            }
            //stop foreground service
            9 -> {
                BaseForegroundService.stop()
            }
            //throw error
            10 -> {
                throw RuntimeException("测试NotificationService异常")
            }
        }
    }

    private fun showInfo() {
        showNotice("permission: ${judge.invoke()}\nstate:${NotificationHelper.isConnected}")
    }
}