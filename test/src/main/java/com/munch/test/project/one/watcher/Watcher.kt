package com.munch.test.project.one.watcher

import android.os.Looper

/**
 * Create by munch1182 on 2021/4/15 16:49.
 */
class Watcher {

    fun watchMainLoop() {
        Looper.getMainLooper().setMessageLogging {
            /*log(it)*/
        }
    }
}