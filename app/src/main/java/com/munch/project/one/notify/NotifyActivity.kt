package com.munch.project.one.notify

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.IconCompat
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.fmt
import com.munch.lib.android.helper.NotificationHelper
import com.munch.lib.android.log.log
import com.munch.lib.android.result.start
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.fastview.fvLLBtn
import com.munch.lib.fast.view.newRandomString
import com.munch.project.one.R
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef

/**
 * Create by munch1182 on 2022/10/20 16:35.
 */
class NotifyActivity : BaseActivity(), ActivityDispatch by dispatchDef(),
    NotificationHelper.OnNotificationListener {

    private val bind by fvLLBtn("start", "stop", "update", "update random", "del", "listen")
    private val notify = NotificationHelper.withService("test notification", 121)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.clickByStr {
            when (it) {
                "start" -> start()
                "stop" -> stop()
                "update" -> update()
                "update random" -> updateRandom()
                "del" -> delFormRandom()
                "listen" -> listen()
            }
        }
    }

    private fun delFormRandom() {
        NotificationHelper.del("test random")
    }

    private fun updateRandom() {
        withCheck {
            NotificationHelper.notify("test random") {
                setSmallIcon(IconCompat.createWithResource(AppHelper, R.mipmap.ic_launcher))
                setContentTitle("随机消息: ${newRandomString()}")
                setContentText(System.currentTimeMillis().fmt())
            }
        }
    }

    private fun listen() {
        NotificationHelper.checkIsEnableListenNotification(this)
            .start {
                if (!it) return@start
                NotificationHelper.add(this)
            }
    }

    override fun onDestroy() {
        super<BaseActivity>.onDestroy()
        NotificationHelper.remove(this)
    }

    private fun start() {
        withCheck { notify.start<NotifyTestService>() }
    }

    private fun withCheck(imp: () -> Unit) {
        NotificationHelper.checkIsEnableNotification(this)
            .start {
                if (!it) return@start
                imp.invoke()
            }
    }

    private fun stop() {
        notify.stop()
    }

    private fun update() {
        notify.notify {
            setSmallIcon(IconCompat.createWithResource(AppHelper, R.mipmap.ic_launcher))
            setContentTitle(newRandomString())
            setContentText(System.currentTimeMillis().fmt())
        }
    }

    class NotifyTestService : Service() {

        override fun onCreate() {
            super.onCreate()
            log("NotifyTestService onCreate")
        }

        override fun onBind(p0: Intent?): IBinder {
            return ServiceBinder()
        }

        inner class ServiceBinder : Binder(), NotificationHelper.IServiceBinder {
            override fun getService(): Service {
                return this@NotifyTestService
            }
        }
    }

    override fun onPost(sbn: StatusBarNotification?) {
        log("onPost:${sbn?.let { "${it.packageName}, ${it.uid}" }}")
    }

    override fun onRemoved(sbn: StatusBarNotification?) {
        log("onRemoved:${sbn?.let { "${it.packageName}, ${it.uid}" }}")
    }
}