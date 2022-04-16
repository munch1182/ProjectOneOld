package com.munch.lib.task

import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.getOrDefault
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create by munch1182 on 2022/4/16 19:34.
 */
interface IDependOrder {

    /**
     * 用于标识该任务属于哪一条有序任务链
     */
    val depend: Array<Key>?
        get() = null
}

interface DependTask : ITask, IDependOrder

internal class TaskDependHandler : TaskHandler {

    private val map = SparseArray<TaskWrapper>()
    private val mapLock = Mutex()

    override suspend fun add(task: TaskWrapper) {
        if (task.task !is IDependOrder) {
            throw IllegalStateException()
        }
        mapLock.withLock {
        }
    }

    override suspend fun run() {
        mapLock.withLock {
        }
    }

    override suspend fun cancel() {
    }
}

internal class TaskDependNotify {

}