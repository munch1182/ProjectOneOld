package com.munch.lib.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Created by munch1182 on 2022/4/17 1:24.
 */
abstract class DialogTask : OrderTask {

    companion object {
        private val KEY_DIALOG = Key(9999)
    }

    override val coroutines: CoroutineContext
        get() = Dispatchers.Default

    override suspend fun run() {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Any?> {
                //todo 显示dialog并设置消失时的回调
            }
        }
    }


    override val orderKey: Key = KEY_DIALOG
}