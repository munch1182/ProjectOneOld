package com.munch.lib.task

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/4/11 16:23.
 */
internal class TaskWrapper(private val task: ITask) {

    val key: Int
        get() = task.key

    var next: TaskWrapper? = null

    var onTaskComplete: OnTaskCompleteListener? = null

    /**
     * 依赖于该任务的任务
     */
    var beDependency = mutableListOf<Int>()

    /**
     * 该任务依赖的任务
     */
    var needRunDepend = task.dependents?.toMutableList()

    /**
     * 当被依赖的任务执行完毕时，会回调此方法
     */
    private suspend fun onDependRun(key: Int, helper: TaskHelper) {
        val needRunDepend = needRunDepend ?: return
        needRunDepend.remove(element = key)
        if (needRunDepend.isEmpty()) {
            run(helper)
        }
    }

    suspend fun run(helper: TaskHelper) {
        ContextScope(task.coroutines).launch {
            delay(task.delayTime)
            try {
                task.run()
                beDependency.forEach { helper.getWrapper(it)?.onDependRun(it, helper) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        onTaskComplete?.onTaskComplete(task.key)
        next?.run(helper)
    }
}