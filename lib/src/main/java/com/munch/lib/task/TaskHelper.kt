package com.munch.lib.task

import android.util.SparseArray
import androidx.core.util.valueIterator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/11 11:49.
 */
class TaskHelper {

    private var isRunning = false

    private val map = SparseArray<TaskWrapper>()

    private var headTask: TaskWrapper? = null

    private var tailTask: TaskWrapper? = null

    fun addTask(task: ITask): TaskHelper {
        TaskScope.launch {
            /*val wrapper = TaskWrapper(task)
            if (headTask == null) {
                headTask = wrapper
            }
            tailTask?.next = wrapper
            tailTask = wrapper*/
            val wrapper = TaskWrapper(task)
            wrapper.needRunDepend?.forEach {
                getWrapper(it)?.beDependency?.add(wrapper.key)
            }
            map.put(task.key.hashCode(), wrapper)
        }
        return this
    }

    /**
     * 开始执行任务
     *
     * 如果有任务未执行，却未处于执行状态，则开始执行任务
     * 如果任务已经在执行中，则不会重复执行
     * 如果任务已经执行完毕，则不会重复执行
     */
    fun run() {
        TaskScope.launch {
            if (isRunning) {
                return@launch
            }
            isRunning = true
            map.valueIterator().forEach { it.run(this@TaskHelper) }
        }
    }

    internal fun getWrapper(key: Int): TaskWrapper? {
        if (map.indexOfKey(key) > -1) {
            return map.get(key)
        }
        return null
    }

    internal object TaskScope : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + SupervisorJob()
    }

}