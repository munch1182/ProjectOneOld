package com.munch.lib.fast.view

import com.munch.lib.android.log.Log
import com.munch.lib.android.log.Logger

/**
 * Create by munch1182 on 2022/9/20 10:42.
 */
class Uncaught : Thread.UncaughtExceptionHandler {

    private val log = Logger()

    override fun uncaughtException(p0: Thread, throwable: Throwable) {
        log.setType(Log.Error).log(throwable)
    }

}