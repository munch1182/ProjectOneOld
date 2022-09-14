package com.munch.lib.android.extend

import android.os.Handler
import android.os.Looper
import com.munch.lib.android.AppHelper
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


/**
 * 当前是否是主线程
 */
inline val isMainThread: Boolean
    get() = Thread.currentThread().let { Looper.getMainLooper().thread.id == it.id }

/**
 * 提供一个全局性的主线程Handler
 */
val main by lazy { Handler(Looper.getMainLooper()) }

/**
 * 保证在主线程执行[doAny]
 */
fun impInMain(doAny: () -> Unit) {
    if (isMainThread) {
        doAny.invoke()
    } else {
        AppHelper.launch(Dispatchers.Main) { doAny.invoke() }
    }
}

/**
 * 合并[CoroutineScope]和[CoroutineContext]
 *
 * 可用于一些使用特殊线程的单例类, 否则使用[CoroutineScope]即可
 */
interface ScopeContext : CoroutineScope, CoroutineContext {

    override fun <R> fold(initial: R, operation: (R, CoroutineContext.Element) -> R): R {
        return coroutineContext.fold(initial, operation)
    }

    override fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? {
        return coroutineContext[key]
    }

    override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext {
        return coroutineContext.minusKey(key)
    }
}

suspend inline fun <T> suspendCancellableCoroutine(
    timeout: Long,
    crossinline block: (CancellableContinuation<T>) -> Unit
): T? = withTimeout(timeout) { suspendCancellableCoroutine(block) }

