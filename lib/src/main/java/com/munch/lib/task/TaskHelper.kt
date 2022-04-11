package com.munch.lib.task

import android.util.ArrayMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/11 11:49.
 */
class TaskHelper {

    private val map = ArrayMap<Key, TaskWrapper>()

    fun add(task: ITask): TaskHelper {
        return this
    }

    internal fun getWrapper(key: Key): TaskWrapper? = null

    internal object TaskScope : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + SupervisorJob()
    }

}