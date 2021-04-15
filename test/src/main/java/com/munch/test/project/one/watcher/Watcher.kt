package com.munch.test.project.one.watcher

import android.os.Looper
import com.munch.pre.lib.log.Logger

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

    fun watchMainLoop() {
        var time = 0L
        Looper.getMainLooper().setMessageLogging {
            if (it.startsWith("<<<<<")) {
                time = System.currentTimeMillis()
            } else if (it.startsWith(">>>>>")) {
                if (time == 0L) {
                    return@setMessageLogging
                }
                val useTime = System.currentTimeMillis() - time
                if (useTime > WATCH_UI_MIN_TIME) {
                    log.log("${useTime}ms")
                }
            }
        }
    }


}