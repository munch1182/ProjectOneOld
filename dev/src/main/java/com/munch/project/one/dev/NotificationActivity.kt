package com.munch.project.one.dev

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.fast.base.BaseBtnWithNoticeActivity
import com.munch.lib.fast.databinding.ItemSimpleBtnWithNoticeBinding
import com.munch.lib.log.log
import com.munch.lib.result.ResultHelper

/**
 * Create by munch1182 on 2021/8/19 9:33.
 */
class NotificationActivity : BaseBtnWithNoticeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isEnable()) {
            showNotice("无获取通知权限")
            setItem(mutableListOf("获取权限"))
        } else {
            showNotify()
            /*enableNotify()*/
        }
    }

    private fun showNotify() {
        showNotice("有获取通知权限")
        setItem(mutableListOf("开启通知权限", "关闭通知权限"))
    }

    private fun enableNotify() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationMonitor.enable(this)
        }*/
        val pm = this.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationMonitor::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationMonitor::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
        log("enableNotify")
    }

    private fun disableNotification() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationMonitor.disable(this)
        }*/
        val pm = this.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(this, NotificationMonitor::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        log("disableNotification")
    }

    override fun onClick(pos: Int, bind: ItemSimpleBtnWithNoticeBinding) {
        super.onClick(pos, bind)
        if (!isEnable()) {
            when (pos) {
                0 -> {
                    ResultHelper.init(this)
                        .with(
                            { isEnable() },
                            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        )
                        .start {
                            if (it) {
                                showNotify()
                            }
                        }
                }
                else -> {
                }
            }
        } else {
            when (pos) {
                0 -> {
                    enableNotify()
                    bind.notice = "success"
                }
                1 -> {
                    disableNotification()
                    bind.notice = "success"
                }
                else -> {
                }
            }
        }
    }

    private fun isEnable() = NotificationManagerCompat.getEnabledListenerPackages(this)
        .contains(packageName)

    class NotificationMonitor : NotificationListenerService() {

        companion object {

            private const val KEY_ENABLE = "KEY_ENABLE"

            @RequiresApi(Build.VERSION_CODES.N)
            fun enable(context: Context) {
                context.startService(Intent(context, NotificationMonitor::class.java).apply {
                    putExtra(KEY_ENABLE, true)
                })
            }

            @RequiresApi(Build.VERSION_CODES.N)
            fun disable(context: Context) {
                context.startService(Intent(context, NotificationMonitor::class.java).apply {
                    putExtra(KEY_ENABLE, false)
                })
            }
        }

        override fun onNotificationPosted(sbn: StatusBarNotification?) {
            super.onNotificationPosted(sbn)
            log("onNotificationPosted")
        }

        override fun onNotificationRemoved(sbn: StatusBarNotification?) {
            super.onNotificationRemoved(sbn)
            log("onNotificationRemoved")
        }

        override fun onListenerConnected() {
            super.onListenerConnected()
            log("onListenerConnected")
        }

        override fun onListenerDisconnected() {
            super.onListenerDisconnected()
            log("onListenerDisconnected")
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            log("onStartCommand")
            if (intent != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (intent.getBooleanExtra(KEY_ENABLE, true)) {
                        requestRebind(ComponentName(this, NotificationMonitor::class.java))
                    } else {
                        requestUnbind()
                    }
                }
            }
            return super.onStartCommand(intent, flags, startId)
        }

    }
}