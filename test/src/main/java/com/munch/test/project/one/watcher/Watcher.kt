package com.munch.test.project.one.watcher

import android.os.*
import com.munch.pre.lib.helper.file.checkOrNew
import com.munch.pre.lib.log.Logger
import java.io.File

/**
 * Create by munch1182 on 2021/4/15 16:49.
 */
class Watcher {

    companion object {
        private const val WATCH_UI_MIN_TIME = 1000L
    }

    private val log = Logger().apply {
        noInfo = true
        tag = "AppWatcher"
    }

    fun watchMainLoop(): Watcher {
        var time = 0L
        Looper.getMainLooper().setMessageLogging {
            if (it.startsWith("<")) {
                time = System.currentTimeMillis()
            } else {
                if (time == 0L) {
                    return@setMessageLogging
                }
                val useTime = System.currentTimeMillis() - time
                if (useTime > WATCH_UI_MIN_TIME) {
                    log.log("${useTime}ms ")
                }
            }
        }
        return this
    }

    /**
     * 严苛模式
     *
     * 输出在Logcat，tag为StrictMode
     */
    fun strictMode(): Watcher {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
        return this
    }

    /**
     * 耗时较长，需要进行文件分析
     */
    fun dumpTrace(file: File): File? {
        val file1 = file.checkOrNew() ?: return null
        Debug.dumpHprofData(file.absolutePath)
        return file1
    }
}