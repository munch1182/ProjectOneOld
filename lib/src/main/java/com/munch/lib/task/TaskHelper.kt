package com.munch.lib.task

import android.util.ArrayMap
import com.munch.lib.log.InfoStyle
import com.munch.lib.log.Logger
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/11 11:49.
 */
class TaskHelper {

    companion object {

        val keyHelper = IDHelper()

        val KEY_COMPLETE = Key(Int.MAX_VALUE)

        internal val log = Logger("task", infoStyle = InfoStyle.THREAD_ONLY)
    }

    private val map = ArrayMap<Key, TaskWrapper?>()
    private val orderHandler by lazy { TaskOrderHandler() }
    private val normalHandler by lazy { TaskNormalHandler() }

    /**
     * 如果一个任务有多种属性，会按照属性的顺序执行
     */
    fun add(task: ITask): TaskHelper {
        runBlocking(TaskScope.coroutineContext) {
            val key = task.key

            val wrapper = TaskWrapper(key, task)
            map[key] = wrapper
            when (task) {
                is ITaskOrder -> {
                    log.log("wrap and dispatch task($key), order(${task.orderKey})")
                    orderHandler.add(wrapper)
                }
                else -> {
                    log.log("wrap and dispatch task($key)")
                    normalHandler.add(wrapper)
                }
            }
        }
        return this
    }

    fun run() {
        TaskScope.launch(TaskScope.coroutineContext) {
            log.log("call TaskHelper.run.")
            normalHandler.run()
            orderHandler.run()
        }
    }

    fun cancel() {
        runBlocking(TaskScope.coroutineContext) {
            normalHandler.cancel()
            orderHandler.cancel()
        }
    }

    internal object TaskScope : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + SupervisorJob()
    }

}