package com.munch.lib.android.task

import com.munch.lib.android.extend.ScopeContext
import com.munch.lib.android.extend.SealedClassToStringByName
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * 任务队列执行
 *
 * 相较于WorkManager, 更为轻便, 增加了协程支持, 增加了依赖任务自动排序和执行, 且只适合App内执行
 *
 * 对于依赖任务:
 *  1. 当被依赖任务已经执行: 当前任务直接执行
 *  2. 当被依赖任务未执行
 *      a. 被依赖任务已经添加: 执行被依赖任务, 被依赖任务执行完成后执行当前任务
 *      b. 被依赖任务未添加: 等待被依赖任务添加、执行后才执行当前任务
 */
class TaskHelper : TaskExecutor {

    private val appJob = SupervisorJob()
    private val appJobName = CoroutineName("App")
    private val config = TaskConfig()
    private val imp = TaskImp(this, config)

    /**
     * 是否并发执行, 即无依赖关系的任务同时执行, 否则则会根据添加顺序和依赖关系逐一执行
     */
    fun isConcurrent(b: Boolean): TaskHelper {
        config.isConcurrent = b
        return this
    }

    fun setOnTaskUpdate(onTaskUpdate: OnTaskUpdate): TaskHelper {
        config.onTaskUpdate = onTaskUpdate
        return this
    }

    override fun add(task: Task): TaskExecutor {
        imp.add(task)
        return this
    }

    override fun run() {
        imp.run()
    }

    override val coroutineContext: CoroutineContext = appJob + appJobName + Dispatchers.Default
}

class TaskConfig {
    var isConcurrent = true
    var onTaskUpdate: OnTaskUpdate? = null
}

typealias OnTaskUpdate = (TaskKey) -> Unit

interface TaskExecutor : ScopeContext {

    fun add(task: Task): TaskExecutor
    fun run()
}

interface Task {

    /**
     * 当前任务的唯一标识
     * 同一标识的任务会被作为是同一任务
     */
    val key: TaskKey
        get() = TaskKey(this::class.java.canonicalName!!)

    /**
     * 任务执行的上下文
     */
    val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined

    /**
     * 在[coroutineContext]上执行当前任务
     *
     * 对于任务来说, 如果返回了[Result]即认为此任务已经执行完毕
     * 因此对于依赖任务来说, 需要同步返回[Result]
     */
    suspend fun run(): Result
}

@JvmInline
value class TaskKey(val key: String)

sealed class Result : SealedClassToStringByName() {
    object Success : Result()
    class Fail(val error: Exception) : Result()
    class Retry(val op: TaskOp) : Result()

    val isSuccess: Boolean
        get() = this == Success
}

sealed class TaskOp : SealedClassToStringByName() {
    /**
     * 即刻重试
     */
    object RetryNow : TaskOp()

    /**
     * 将失败的任务放在队列末尾等待再次执行
     */
    object RetryEnd : TaskOp()
}

sealed class TaskState : SealedClassToStringByName() {
    // 等待执行
    object Wait : TaskState()

    // 执行中
    object Executing : TaskState()

    // 执行成功
    object Success : TaskState()

    // 执行失败
    object Fail : TaskState()

    val isSuccess: Boolean
        get() = this == Success

    val isExecuting: Boolean
        get() = this == Executing
}