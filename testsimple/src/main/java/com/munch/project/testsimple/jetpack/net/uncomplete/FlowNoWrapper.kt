package com.munch.project.testsimple.jetpack.net.uncomplete

import com.munch.lib.UNCOMPLETE
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

/**
 * @see [kotlinx.coroutines.flow.FlowKt.flow]
 * @see [kotlinx.coroutines.flow.SafeFlow]
 * Create by munch1182 on 2020/12/18 16:16.
 */
@UNCOMPLETE
class FlowNoWrapper<T>(private val block: suspend FlowCollector<T>.() -> Unit) : Flow<T> {
    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) {
        collector.block()
    }
}