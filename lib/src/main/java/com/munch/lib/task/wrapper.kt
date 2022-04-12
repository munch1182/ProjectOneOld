package com.munch.lib.task

import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/4/11 16:23.
 */

internal open class TaskWrapper(val task: ITask) : ITask by task {
    var state: State = State.Wait
        private set
}

internal abstract class BaseTaskHandler {
    var state: State = State.Wait

    abstract suspend fun add(task: TaskWrapper)

    abstract suspend fun run(helper: TaskHelper)
}

internal class NormalTaskHandler : BaseTaskHandler() {

    override suspend fun add(task: TaskWrapper) {
        ContextScope(task.coroutines).launch {
            try {
                task.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun run(helper: TaskHelper) {
    }
}

internal class OrderTaskWrapper(task: TaskWrapper) : TaskWrapper(task) {

    var next: OrderTaskWrapper? = null
}

internal class OrderTaskHandler : BaseTaskHandler() {

    var headTask: OrderTaskWrapper? = null
    var tailTask: OrderTaskWrapper? = null

    override suspend fun add(task: TaskWrapper) {
        val wrapper = OrderTaskWrapper(task)

        if (headTask == null) {
            headTask = wrapper
        }
        tailTask?.next = wrapper
        tailTask = wrapper
    }

    override suspend fun run(helper: TaskHelper) {
        val task = headTask ?: return
        ContextScope(task.coroutines).launch {
            try {
                task.run()
                task.next?.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

internal class DependentTaskWrapper(task: TaskWrapper) : TaskWrapper(task) {

    /**
     * 依赖于此任务的任务
     */
    private var dependentTask = mutableListOf<Key>()
    private var dependCount = task.depends?.size ?: 0

    suspend fun dependNotify(helper: TaskHelper) {
        dependentTask.forEach {
            (helper.getWrapper(it) as? DependentTaskWrapper)?.onDependNotify()
        }
    }

    private suspend fun onDependNotify() {
        dependCount--
        if (dependCount <= 0) {
            ContextScope(task.coroutines).launch {
                task.run()
            }
        }
    }
}

internal class DependentTaskHandler : BaseTaskHandler() {

    private val list = mutableListOf<DependentTaskWrapper>()

    override suspend fun add(task: TaskWrapper) {
        list.add(DependentTaskWrapper(task))
    }

    override suspend fun run(helper: TaskHelper) {
        list.forEach {
            it.run()
            it.dependNotify(helper)
        }
    }

}