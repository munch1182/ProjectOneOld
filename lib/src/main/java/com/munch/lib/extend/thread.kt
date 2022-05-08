@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.munch.lib.helper.ThreadHelper
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min


/**
 * Create by munch1182 on 2021/11/8 16:03.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Thread.isMain() = Looper.getMainLooper().thread.id == this.id

/**
 * 相较于系统的thread，增加了loop
 */
fun thread(
    start: Boolean = true,
    isDaemon: Boolean = false,
    contextClassLoader: ClassLoader? = null,
    name: String? = null,
    priority: Int = -1,
    loop: Boolean = false,
    block: () -> Unit
): Thread {
    val thread = object : Thread() {
        override fun run() {
            if (loop) {
                Looper.prepare()
            }
            block()
            if (loop) {
                Looper.loop()
            }
        }
    }
    if (isDaemon)
        thread.isDaemon = true
    if (priority > 0)
        thread.priority = priority
    if (name != null)
        thread.name = name
    if (contextClassLoader != null)
        thread.contextClassLoader = contextClassLoader
    if (start)
        thread.start()
    return thread
}

inline fun pool(submit: Boolean = false, noinline block: () -> Unit) {
    ThreadHelper.pool(submit, block)
}

inline fun pool(submit: Boolean = false, task: Runnable) {
    ThreadHelper.pool(submit, task)
}

inline fun <T> pool(task: Callable<T>): Future<T>? = ThreadHelper.pool(task)

inline fun postUI(runnable: Runnable) = ThreadHelper.mainHandler.post(runnable)
inline fun postUI(time: Long, runnable: Runnable) =
    ThreadHelper.mainHandler.postDelayed(runnable, time)

open class ThreadHandler private constructor(loop: Looper) : Handler(loop) {

    constructor(name: String) : this(HandlerThread(name).apply { start() }.looper)

    val thread: HandlerThread
        get() = looper.thread as HandlerThread
}
