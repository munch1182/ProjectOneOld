package com.munch.test.project.one.net

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.service.BaseForegroundService
import com.munch.pre.lib.log.log
import com.munch.test.project.one.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Create by munch1182 on 2021/4/25 15:12.
 */
class NetClipService : BaseForegroundService(Parameter("netclipservice", "netclip", 1607)) {

    companion object {

        fun start(context: Context) {
            startForegroundService(context, Intent(context, NetClipService::class.java))
        }

        fun stop(context: Context) {
            stop(context, Intent(context, NetClipService::class.java))
        }

    }

    private var netClipHelper: NetClipHelper? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val instance = NetClipHelper.getInstance()
        if (instance != netClipHelper) {
            clear()
            netClipHelper = instance
            listener()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val t: (content: String, ip: String) -> Unit = { message, _ ->
        log(message)
        runBlocking(Dispatchers.Main) {
            AppHelper.put2Clip(text = message)
            val content = if (message.length > 5) message.subSequence(0, 5).toString()
                .plus("...") else message
            Toast.makeText(this@NetClipService, "${content}已被复制到剪切板", Toast.LENGTH_SHORT).show()
        }
    }
    private val state: (state: Int) -> Unit = {
        if (NetClipHelper.isClosed(it)) {
            stopForeground(true)
            stopSelf()
        }
    }


    private fun listener() {
        if (netClipHelper == null) {
            return
        }
        netClipHelper!!.messageListener.add(t)
        netClipHelper!!.stateListener.add(state)
    }

    private fun clear() {
        netClipHelper?.messageListener?.remove(t)
        netClipHelper?.stateListener?.remove(state)
    }

    override fun onDestroy() {
        super.onDestroy()
        clear()
    }

    override fun buildNotification(title: String?): NotificationCompat.Builder {
        return super.buildNotification(title ?: "剪切板服务").setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    111,
                    Intent(this, NetClipActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setContentText("局域网剪切板服务正在后台运行中")
    }

}