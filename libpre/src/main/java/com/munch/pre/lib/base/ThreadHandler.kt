package com.munch.pre.lib.base

import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.Executor

/**
 * Create by munch1182 on 2021/5/13 10:12.
 */
object ThreadHandler {

    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    private val executor by lazy { ThreadExecutor(getHandler()) }

    fun start() {
        if (thread == null) {
            thread = HandlerThread("BASE_HANDLER_THREAD")
        }
        thread?.start()
        handler = Handler(thread!!.looper)
    }

    fun stop() {
        thread?.quitSafely()
        thread = null
        handler = null
    }

    fun getHandler(): Handler {
        return handler ?: throw IllegalStateException("must call start first")
    }

    fun execute(runnable: Runnable) {
        executor.execute(runnable)
    }

    internal class ThreadExecutor(private val handler: Handler) : Executor {
        override fun execute(command: Runnable) {
            handler.post(command)
        }
    }
}