package com.munch.lib.android.helper

import com.munch.lib.android.log.log

/**
 * Create by munch1182 on 2022/9/20 10:22.
 */
object ThreadHelper {

    /**
     * 异常处理: 打印日志
     */
    private val defCaught by lazy {
        Thread.UncaughtExceptionHandler { _, throwable ->
            log(throwable)
            // 会造成异常的循环
            // Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
    }
    private var setCaught: Thread.UncaughtExceptionHandler? = null
    private val caught: Thread.UncaughtExceptionHandler
        get() = setCaught ?: defCaught

    /**
     * 捕获当前线程的未捕获异常
     *
     * @see setCaught
     */
    fun caughtThreadException() {
        Thread.setDefaultUncaughtExceptionHandler(caught)
    }

    /**
     * @see caughtThreadException
     */
    fun setCaught(handler: Thread.UncaughtExceptionHandler): ThreadHelper {
        this.setCaught = handler
        return this
    }

}