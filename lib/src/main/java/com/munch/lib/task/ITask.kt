package com.munch.lib.task

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/4/11 11:48.
 */
interface ITask {

    /**
     * 该任务的唯一标识
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

    suspend fun run()

    /**
     * 当调用取消时此任务未执行完毕，则会被回调此方法
     * 需要在此处进行取消任务并返回取消结果
     */
    suspend fun cancel(): Boolean = true
}

//todo 观测回调
sealed class State {

    object Wait : State()
    object Executing : State()
    object Complete : State()
    object Cancel : State()

    val isWait: Boolean
        get() = this is Wait
    val isExecuting: Boolean
        get() = this is Executing
    val isComplete: Boolean
        get() = this is Complete
    val isCancel: Boolean
        get() = this is Cancel
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

    override fun toString() = "task $key"
}

internal open class TaskWrapper(val task: ITask) : ITask by task {
    var state: State = State.Wait
        set(value) {
            val old = field
            field = value
            TaskHelper.log.log("$key state: $old -> $value.")
        }

    override suspend fun run() {
        TaskHelper.log.log("$key run")
        task.run()
    }
}