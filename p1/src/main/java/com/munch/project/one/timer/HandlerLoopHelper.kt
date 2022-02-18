package com.munch.project.one.timer

import android.os.Handler
import android.os.Looper
import com.munch.lib.app.AppForegroundHelper
import com.munch.lib.app.AppHelper
import com.munch.lib.app.OnAppForegroundChangeListener
import com.munch.lib.log.Log2FileHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create by munch1182 on 2022/2/18 15:16.
 */
object HandlerLoopHelper : Runnable, OnAppForegroundChangeListener {

    private val handler = Handler(Looper.getMainLooper())

    private var time: Long = 0
    private const val TIME15 = 15 * 60 * 1000L

    fun start() {
        handler.removeCallbacks(this)
        handler.post(this)
        AppForegroundHelper.remove(this)
        AppForegroundHelper.add(this)
    }

    override fun run() {
        val context = AppHelper.appNull ?: return
        val now = System.currentTimeMillis()
        val content =
            "${"HH:mm:ss".format(now)} 是否在前台: ${AppForegroundHelper.isInForeground}, 是否锁屏: ${AppForegroundHelper.isScreenOn()}"
        Log2FileHelper(File(context.cacheDir, "yyyyMMdd".format(now))).write(content)
    }


    override fun invoke(isInForeground: Boolean) {
        checkNow()
    }

    fun checkNow() {
        val now = System.currentTimeMillis()
        if (now - time > TIME15) {
            time = now
            start()
        }
    }
}

private fun String.format(time: Long): String {
    return SimpleDateFormat(this, Locale.getDefault()).format(time)
}