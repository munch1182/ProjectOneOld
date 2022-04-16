package com.munch.lib.task

/**
 * Create by munch1182 on 2022/4/16 19:29.
 */
internal interface TaskHandler {

    suspend fun add(task: TaskWrapper)

    suspend fun run()

    suspend fun cancel() {
    }
}

internal class TaskNormalHandler : TaskHandler {

    override suspend fun add(task: TaskWrapper) {
        task.run()
    }

    override suspend fun run() {
    }
}