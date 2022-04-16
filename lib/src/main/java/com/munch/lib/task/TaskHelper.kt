package com.munch.lib.task

import android.util.ArrayMap
import com.munch.lib.log.InfoStyle
import com.munch.lib.log.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/11 11:49.
 */
class TaskHelper {

    companion object {

        internal val num = IDHelper()

        internal val log = Logger("task", infoStyle = InfoStyle.THREAD_ONLY)
    }

    private val map = ArrayMap<Key, TaskWrapper?>()
    private val mapLock = Mutex()
    private val orderHandler by lazy { TaskOrderHandler() }
    private val normalHandler by lazy { TaskNormalHandler() }

    /**
     * 如果一个任务有多种属性，会按照属性的顺序执行
     */
    fun add(task: ITask): TaskHelper {
        // todo 其它方式保证执行顺序
        runBlocking {
            val wrapper = TaskWrapper(task)
            mapLock.withLock { map[wrapper.key] = wrapper }
            when (task) {
                is ITaskOrder -> orderHandler.add(wrapper)
                else -> normalHandler.add(wrapper)
            }
        }
        return this
    }

    fun run() {
        TaskScope.launch {
            normalHandler.run()
            orderHandler.run()
        }
    }

    fun cancel() {
        runBlocking {
            normalHandler.cancel()
            orderHandler.cancel()
        }
    }

    internal object TaskScope : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + SupervisorJob()
    }

}