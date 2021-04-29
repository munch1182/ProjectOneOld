package com.munch.test.project.one.net.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.NetworkCapabilities
import androidx.core.app.NotificationCompat
import com.munch.pre.lib.helper.NetStatusHelper
import com.munch.pre.lib.helper.service.BaseForegroundService
import com.munch.test.project.one.R
import com.munch.test.project.one.net.NetClipActivity

/**
 * Create by munch1182 on 2021/4/29 16:18.
 */
class NetKeepService : BaseForegroundService(Parameter("net service", "keep", 429)) {

    companion object {

        fun start(context: Context) {
            startForegroundService(context, Intent(context, NetKeepService::class.java))
        }

        fun stop(context: Context) {
            stop(context, Intent(context, NetKeepService::class.java))
        }

        fun notify(context: Context) {
            start(context)
        }

    }

    private val instance by lazy { NetStatusHelper.getInstance(this) }
    private val service by lazy { AndServiceHelper.INSTANCE }
    private val listener: (available: Boolean, capabilities: NetworkCapabilities?) -> Unit =
        { a, _ -> notify(a) }

    override fun onCreate() {
        super.onCreate()
        instance.add(listener)
        instance.limitTransportType(NetworkCapabilities.TRANSPORT_WIFI).register()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance.remove(listener)
        instance.unregister()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notify(NetStatusHelper.wifiAvailable(this))
        return super.onStartCommand(intent, flags, startId)
    }

    private fun notify(available: Boolean) {
        notify(buildNotification(null).setContentText(getContent(available)).build())
    }


    private fun getContent(available: Boolean) = if (!available) {
        "wifi已关闭"
    } else {
        if (service.isRunning()) {
            "局域网服务器服务(${getIp()})正在后台运行中"
        } else {
            "WIFI已开启"
        }
    }

    override fun buildNotification(title: String?): NotificationCompat.Builder {
        return super.buildNotification(title ?: "服务器服务").setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    111,
                    Intent(this, NetClipActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setContentText("服务已启动")
    }

    private fun getIp(): String {
        return "${NetStatusHelper.getIpAddress() ?: return "ip获取失败"}:${AndServiceHelper.PORT}"
    }
}