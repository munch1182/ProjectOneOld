package com.munch.lib.fast.base

import android.os.Looper
import com.munch.lib.app.AppForegroundHelper
import com.munch.lib.app.AppHelper
import com.munch.lib.base.destroy
import com.munch.lib.fast.dialog.SimpleDialog
import com.munch.lib.helper.toDate
import com.munch.lib.log.Log2FileHelper
import com.munch.lib.log.log
import com.munch.lib.task.handler
import com.munch.lib.task.isMain
import java.io.File

/**
 * Create by munch1182 on 2021/11/24 15:49.
 */
object ExceptionCatchHandler : Thread.UncaughtExceptionHandler {

    private val dir by lazy { File(AppHelper.app.cacheDir, "/e") }
    private val log by lazy { Log2FileHelper(dir) }

    val exceptionFileDir: File
        get() = dir

    fun handle() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        log(e)
        if (t.isMain()) {
            /*thread(loop = true) { showDialog(t, e) }*/
            showDialog(t,e)
            Looper.loop()
        } else {
            handler { showDialog(t, e) }
        }
    }

    private fun showDialog(t: Thread, e: Throwable) {
        log.write("${System.currentTimeMillis().toDate()}:")
        log.write(e)
        AppForegroundHelper.currentActivity?.let {
            SimpleDialog(it)
                .setName("提示")
                .setContent("error: \n${e.localizedMessage}\n${t.name}")
                .setOnSureListener("重启") { d ->
                    d.cancel()
                    destroy()
                }
                .show(!t.isMain())
        }
    }
}