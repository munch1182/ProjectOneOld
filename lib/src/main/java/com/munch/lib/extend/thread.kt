@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import com.munch.lib.helper.ThreadHelper
import kotlinx.coroutines.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import kotlin.coroutines.CoroutineContext


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

inline fun postUI(time: Long = 0L, runnable: Runnable) =
    ThreadHelper.mainHandler.postDelayed(runnable, time)

inline fun delay(time: Long = 0L, runnable: Runnable) {
    if (Thread.currentThread().isMain()) {
        postUI(time, runnable)
    } else {
        SystemClock.sleep(time)
        runnable.run()
    }
}

open class ThreadHandler private constructor(loop: Looper) : Handler(loop) {

    constructor(name: String) : this(HandlerThread(name).apply { start() }.looper)

    val thread: HandlerThread
        get() = looper.thread as HandlerThread
}

/**
 * @see kotlinx.coroutines.android.HandlerContext
 */
open class HandlerDispatcher(name: String) : CoroutineDispatcher() {

    open val handler = ThreadHandler(name)

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return Looper.myLooper() != handler.looper
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!handler.post(block)) {
            cancelOnRejection(context, block)
        }
    }

    private fun cancelOnRejection(context: CoroutineContext, block: Runnable) {
        context.cancel(CancellationException("The task was rejected, the handler underlying the dispatcher '${toString()}' was closed"))
        Dispatchers.IO.dispatch(context, block)
    }

    override fun toString(): String = "HandlerDispatcher"

    override fun equals(other: Any?): Boolean =
        other is HandlerDispatcher && other.handler === handler

    override fun hashCode(): Int = System.identityHashCode(handler)
}

class ContextScope(job: Job, context: CoroutineContext) : CoroutineScope, Job by job {
    override val coroutineContext: CoroutineContext = job + context

    // CoroutineScope is used intentionally for user-friendly representation
    override fun toString(): String = "CoroutineScope(coroutineContext=$coroutineContext)"
}

