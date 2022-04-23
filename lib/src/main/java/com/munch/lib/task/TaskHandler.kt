package com.munch.lib.task

import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/4/16 19:29.
 */
internal interface TaskHandler {

    suspend fun add(task: TaskWrapper)

    suspend fun run()

    suspend fun cancel() {
    }

    suspend fun pause() {}
}

internal class TaskNormalHandler : TaskHandler {

    override suspend fun add(task: TaskWrapper) {
        TaskHelper.TaskScope.launch(task.coroutines) { task.run() }
    }

    override suspend fun run() {
    }
}