package com.munch.lib.bluetooth

import android.os.Looper
import com.munch.lib.extend.ThreadHandler
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * @see kotlinx.coroutines.android.HandlerContext
 */
internal class BluetoothDispatcher : CoroutineDispatcher() {

    internal val handler = ThreadHandler("bluetooth_thread")

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

    override fun toString(): String = "BluetoothDispatcher"

    override fun equals(other: Any?): Boolean =
        other is BluetoothDispatcher && other.handler === handler

    override fun hashCode(): Int = System.identityHashCode(handler)

}