package com.munch.lib.task

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/11 11:48.
 */
interface ITask {

    /**
     * 用于标识该任务的唯一标识
     *
     * 如果两个任务标识一致，在未执行的状态下后添加任务会覆盖前任务
     */
    val key: Key

    /**
     * 执行此任务前的等待时间
     */
    val delayTime: Long
        get() = 0L

    /**
     * 此任务执行的上下文
     */
    val coroutines: CoroutineContext
        get() = Dispatchers.Default

    /**
     * 依赖的任务
     */
    val depends: Array<Int>?
        get() = null

    suspend fun run()
}

data class Key(private val key: Int)

sealed class State {
    object Wait : State()
    object Executing : State()
    object Completed : State()
}

interface OnTaskCompleteListener {

    fun onTaskComplete(key: Int, exception: Exception? = null)
}