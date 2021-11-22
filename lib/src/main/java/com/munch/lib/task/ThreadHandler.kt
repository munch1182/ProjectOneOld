package com.munch.lib.task

import android.os.Handler
import android.os.Handler.Callback
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import java.io.Closeable

/**
 * Create by munch1182 on 2021/11/9 10:01.
 */
class ThreadHandler(looper: Looper, callback: Callback? = null) : Handler(looper, callback),
    Closeable {

    constructor(
        name: String,
        callback: Callback? = null
    ) : this(HandlerThread(name).apply { start() }.looper, callback)

    constructor(name: String, callback: HandlerCallBack? = null) : this(name,
        Callback { msg ->
            callback?.invoke(msg)
            true
        })

    private val th by lazy { looper.thread as HandlerThread }

    override fun close() {
        quit()
    }

    fun quit() = th.quit()
    fun quitSafely() = th.quitSafely()
}

typealias HandlerCallBack = (msg: Message) -> Unit

val mainHandler by lazy { Handler(Looper.getMainLooper()) }

@Suppress("NOTHING_TO_INLINE")
inline fun handler(delay: Long = 0L, r: Runnable) {
    mainHandler.postDelayed(r, delay)
}