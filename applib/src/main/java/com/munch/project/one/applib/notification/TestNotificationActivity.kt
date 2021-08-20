package com.munch.project.one.applib.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.munch.lib.fast.base.BaseBtnWithNoticeActivity
import com.munch.lib.fast.databinding.ItemSimpleBtnWithNoticeBinding
import com.munch.lib.log.log
import com.munch.lib.notification.NotificationService
import com.munch.project.one.applib.R

/**
 * Create by munch1182 on 2021/8/19 11:13.
 */
class TestNotificationActivity : BaseBtnWithNoticeActivity() {

    private var keep = false
    private var byHand = false
    private val channelId = "test_channel"
    private val nm by lazy { NotificationManagerCompat.from(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!NotificationService.isEnable()) {
            set("无通知权限", mutableListOf("获取权限"))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationService.disable()
                showItem()
            } else {
                set("有通知权限，但sdk小于24，因此无法关闭")
            }
        }
        NotificationService.registerOnNotificationChangeListener { log("onChange") }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    private fun showItem() {
        set(
            "有通知权限",
            mutableListOf(
                "开始接收通知",
                "不再接收通知",
                if (!keep) "退出时仍然接收通知" else "退出时关闭接收通知",
                "发出一个通知",
                "取消通知"
            )
        )
    }

    override fun onClick(pos: Int, bind: ItemSimpleBtnWithNoticeBinding) {
        super.onClick(pos, bind)
        if (!NotificationService.isEnable()) {
            if (pos == 0) {
                startActivity(NotificationService.requestIntent())
            }
        } else {
            when (pos) {
                0 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        NotificationService.enable()
                        byHand = true
                        bind.notice = "已开始接收通知"
                    }
                }
                1 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        NotificationService.disable()
                        bind.notice = "已关闭接收通知"
                    }
                }
                2 -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        keep = !keep
                        bind.text = if (!keep) "退出时仍然接收通知" else "退出时关闭接收通知"
                    }
                }
                3 -> {
                    nm.notify(
                        819,
                        NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(System.currentTimeMillis().toString())
                            .build()
                    )
                }
                4 -> {
                    nm.cancelAll()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!keep) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationService.disable()
            }
        }
        NotificationService.unregisterOnNotificationChangeListener()
    }
}