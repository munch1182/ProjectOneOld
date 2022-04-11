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
     * 如果两个任务标识一致，则会使用后添加任务覆盖前任务
     */
    val key: Int
        get() = this.hashCode()

    /**
     * 执行此任务前的等待时间
     */
    val delayTime: Long
        get() = 0L

    /**
     * 此任务执行的上下文
     */
    val coroutines: CoroutineContext
        get() = Dispatchers.Default + CoroutineName(key.toString())

    /**
     * 此任务的依赖任务
     *
     * @see key
     * @see isRunIfNoDepend
     */
    val dependents: Array<Int>?
        get() = null

    /**
     * 当前TaskHelper内，如果依赖任务未执行过，此任务是否执行
     *
     * 如果不执行，则会等待直到依赖任务被添加并执行
     *
     * @see dependents
     */
    val isRunIfNoDepend: Boolean
        get() = false

    suspend fun run()
}

interface OnTaskCompleteListener {

    fun onTaskComplete(key: Int, exception: Exception? = null)
}