package com.munch.lib.task

import android.util.ArrayMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/11 11:49.
 */
class TaskHelper {

    companion object {

        internal val num = NumberHelper()
    }

    private val map = ArrayMap<Key, TaskWrapper?>()
    private val orderHandler by lazy { OrderTaskHandler() }
    private val dependentHandler by lazy { DependentTaskHandler() }
    private val normalHandler by lazy { NormalTaskHandler() }

    fun add(task: ITask): TaskHelper {
        TaskScope.launch {
            val wrapper = TaskWrapper(task)
            map[task.key] = wrapper
            when (task) {
                is IOrdered -> orderHandler.add(wrapper)
                is IDependent -> dependentHandler.add(wrapper)
                else -> normalHandler.add(wrapper)
            }
        }
        return this
    }

    fun run() {
        TaskScope.launch {
            if (!normalHandler.isExecuting()) {
                normalHandler.run(this@TaskHelper)
            }
            if (!orderHandler.isExecuting()) {
                orderHandler.run(this@TaskHelper)
            }
            if (!dependentHandler.isExecuting()) {
                dependentHandler.run(this@TaskHelper)
            }
        }
    }

    internal fun getWrapper(key: Key): TaskWrapper? = map.getOrDefault(key, null)

    internal object TaskScope : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + SupervisorJob()
    }

}