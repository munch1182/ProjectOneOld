package com.munch.project.one.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.HandlerThread
import android.os.Parcelable
import com.munch.lib.app.AppHelper
import com.munch.lib.base.OnReceive
import com.munch.lib.log.Logger
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

/**
 * 只负责接收的数据部分
 *
 * Create by munch1182 on 2021/10/15 17:13.
 */
class LogReceiveHelper private constructor() {

    companion object {

        val INSTANCE by lazy { LogReceiveHelper() }

        private const val HT_NAME = "log_receive_handler"

        const val BROADCAST_PERMISSION = "com.munch.BROADCAST_PERMISSION"
        const val KEY_LOG_BEAN = "log"

        internal val log = Logger("log-receive")
    }

    //<editor-fold desc="state">
    private var isRunningVal = false

    val isRunning: Boolean
        get() = isRunningVal
    //</editor-fold>

    //<editor-fold desc="data">
    private val logs: MutableList<LogBean> = mutableListOf()
    var onReceived: OnReceive<LogBean>? = null
    private val onReceivedVal: OnReceive<LogBean> = {
        logs.add(it)
        onReceived?.invoke(it)
    }
    //</editor-fold>


    //<editor-fold desc="system">

    private val receiver = LogBroadcastReceiver()
    private var logHandlerThread: HandlerThread? = null
    private var handler: Handler? = null

    fun start(vararg actions: String) {
        if (isRunningVal) {
            stop()
        }
        isRunningVal = true
        logHandlerThread = HandlerThread(HT_NAME)
        logHandlerThread!!.start()
        handler = Handler(logHandlerThread!!.looper)
        val intentFilter = IntentFilter()
        actions.forEach { intentFilter.addAction(it) }
        receiver.onReceive = onReceivedVal
        AppHelper.app.registerReceiver(receiver, intentFilter, BROADCAST_PERMISSION, handler!!)
    }

    fun stop() {
        isRunningVal = false
        try {
            receiver.onReceive = null
            AppHelper.app.unregisterReceiver(receiver)
            logHandlerThread?.quit()
            logHandlerThread = null
            handler = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //</editor-fold>

}

internal class LogBroadcastReceiver(var onReceive: OnReceive<LogBean>? = null) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val content = it.getStringExtra(LogReceiveHelper.KEY_LOG_BEAN) ?: ""
            val from = it.action ?: "unKnow"
            onReceive?.invoke(LogBean(content, from, System.currentTimeMillis()))
        }
    }
}

@Parcelize
data class LogBean(val content: String, val from: String, val receiveTime: Long) : Parcelable {

    fun toStr(formatTime: Boolean = false): String {
        return if (formatTime) {
            val time =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(receiveTime)
            "$time: $content"
        } else {
            content
        }
    }
}