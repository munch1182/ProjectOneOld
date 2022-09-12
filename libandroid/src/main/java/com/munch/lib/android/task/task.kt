package com.munch.lib.android.task

import com.munch.lib.android.function.Notify
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.android.log.LogInfo
import com.munch.lib.android.log.Logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

class TaskWrapper(private val task: Task) {

    private var executeState: TaskState = TaskState.Wait

    private var ars: ARSHelper<Notify>? = null

    val key: TaskKey = task.key

    val isExecuting: Boolean
        get() = executeState.isExecuting

    suspend fun run(): Result {
        return withContext(task.coroutineContext) {
            val result = task.run()
            if (result.isSuccess) {
                ars?.apply {
                    update { it.invoke() }
                    clear()
                }
            }
            return@withContext result
        }
    }

    /**
     * 如果有任务依赖于此任务, 则可以注册此任务完成回调
     * 当此任务完成后, 会通知依赖的任务完成
     */
    fun registerUpdate(notify: Notify) {
        if (executeState.isSuccess) {
            notify.invoke()
        } else {
            if (ars == null) {
                ars = ARSHelper()
            }
            ars?.add(notify)
        }
    }
}

/**
 * 实现TaskExecutor的逻辑
 * 1. add由TaskWrapper包装的task
 * 2. 运行是由TaskWrapper分配线程调用执行
 * 3. 由此类处理运行结果并记录
 */
class TaskImp(
    override val coroutineContext: CoroutineContext,
    private val config: TaskConfig
) : TaskExecutor {

    private val list = LinkedList<TaskWrapper>()
    private val logger = Logger("task", LogInfo.Thread)

    override fun add(task: Task): TaskExecutor {
        val wrapper = TaskWrapper(task)
        list.add(wrapper)
        logger.log("add task ${wrapper.key}.")
        return this
    }

    override fun run() {
        launch {
            logger.log("execute run with task ${list.size}")
            while (!list.isEmpty()) {
                val curr = list.pop()
                when (val result = curr.run()) {
                    is Result.Retry -> {
                        when (result.op) {
                            TaskOp.RetryEnd -> {
                                list.addLast(curr)
                            }
                            TaskOp.RetryNow -> {
                                list.addFirst(curr)
                            }
                        }
                    }
                    is Result.Fail -> {}
                    Result.Success -> {}
                }
            }
        }
    }
}