package com.munch.lib.android.extend

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

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