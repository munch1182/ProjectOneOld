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
        get() = Key(10000 + TaskHelper.num.curr)

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

data class Key(private val key: Int) {

    override fun hashCode(): Int {
        return key
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Key

        if (key != other.key) return false

        return true
    }
}

interface IDependent {
    val dependent: Array<Key>?
        get() = null
}

interface IOrdered {
    val order: OrderKey
}

data class OrderKey(private val key: Int)

sealed class State {
    object Wait : State()
    object Executing : State()
    object Completed : State()
}

interface OnTaskCompleteListener {

    fun onTaskComplete(key: Int, exception: Exception? = null)

}