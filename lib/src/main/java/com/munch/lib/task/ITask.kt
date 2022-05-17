package com.munch.lib.task

import com.munch.lib.Key
import com.munch.lib.helper.IInterHelper
import com.munch.lib.helper.InterHelper
import com.munch.lib.helper.data.DataFun
import com.munch.lib.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

/**
 * 所有的任务都应该有结果，也应该考虑进度
 *
 * Create by munch1182 on 2022/4/11 11:48.
 */
interface ITask {

    /**
     * 该任务的唯一标识
     *
     * 如果两个任务标识一致，在未执行的状态下后添加任务会覆盖前任务
     */
    val key: Key

    /**
     * 此任务执行的上下文
     */
    val coroutines: CoroutineContext
        get() = Dispatchers.Default

    /**
     *
     * 当需要该任务执行时会回调此方法，需要返回任务执行的结果
     * 如果此方法不同步执行，则会在任务未完成时即返回任务的结果
     *
     * @param input 任务传递的参数，如果不使用此对象，传递的参数的消失
     * @return 执行结果
     *
     * @see Result
     * @see Data
     */
    suspend fun run(input: Data?): Result

    /**
     * 当调用取消时此任务未执行完毕，则会被回调此方法
     * 需要在此处进行取消任务并返回取消结果
     */
    suspend fun cancel(input: Data?): Boolean = true
}

abstract class Task : ITask {

    override val key: Key = Key(TaskKeyHelper.curr)
}

sealed class State {

    object Wait : State() {
        override fun toString() = "WAIT"
    }

    object Executing : State() {
        override fun toString() = "EXECUTING"
    }

    object Complete : State() {
        override fun toString() = "COMPLETE"
    }

    object Cancel : State() {
        override fun toString() = "CANCEL"
    }

    val isCancel: Boolean
        get() = this is Cancel

    val needRun: Boolean
        get() = this is Wait

    val can2Cancel: Boolean
        get() = this is Wait || this is Executing
}

object TaskKeyHelper : IInterHelper by InterHelper()

sealed class Result {

    internal open var data: Data? = null

    internal fun with(data: Data?): Result {
        this.data = data
        return this
    }

    internal object Success : Result() {
        override fun toString() = "SUCCESS"
    }

    internal class Failure(private val e: Exception? = null) : Result() {
        override fun toString() = "FAILURE"
    }

    internal object Invalid : Result()

    internal object Retry : Result() {
        override fun toString() = "RETRY"
    }

    internal val isSuccess: Boolean
        get() = this is Success

    internal val isFailure: Boolean
        get() = this is Failure

    internal val needRetry: Boolean
        get() = this is Retry

    companion object {

        fun success(data: Data? = null) = Success.with(data)
        fun failure(data: Data? = null, e: Exception? = null) = Failure(e).with(data)
        fun retry(data: Data? = null) = Retry.with(data)
    }
}

class Data(hashMap: HashMap<String, Any?>? = null) : DataFun<String> {

    constructor(data: Data?) : this(data?.map?.let { HashMap(it) })

    private var map: HashMap<String, Any?> = hashMap?.let { HashMap(it) } ?: hashMapOf()

    override fun put(key: String, value: Any?) {
        map[key] = value
    }

    override fun remove(key: String): Boolean {
        map.remove(key)
        return true
    }

    override fun <T> get(key: String, defValue: T?): T? {
        @Suppress("UNCHECKED_CAST")
        return if (hasKey(key)) map[key] as? T else defValue
    }

    override fun clear() {
        map.clear()
    }

    override fun hasKey(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun toMap(): Map<String, Any?> = map
}

open class TaskWrapper(
    override val key: Key,
    val task: ITask,
    private val log: Logger
) : ITask by task {

    private val lock = Mutex()
    private var state: State = State.Wait
        set(value) {
            runBlocking {
                lock.withLock {
                    val old = field
                    field = value
                    log.log { "task $key: $old -> $field" }
                }
            }
        }
        get() = runBlocking { lock.withLock { field } }

    override suspend fun run(input: Data?): Result {
        //如果任务已被取消，但仍被调用执行，则直接返回执行失败
        if (!state.needRun) {
            return Result.Invalid
        }
        return try {
            state = State.Executing
            log.log { "task $key run." }
            var run = task.run(input)
            while (run.needRetry) {
                log.log { "task $key retry." }
                run = task.run(input)
            }
            log.log { "task $key complete." }
            state = State.Complete
            run
        } catch (e: Exception) {
            state = State.Cancel
            log.log { "task $key cancel because of exception." }
            Result.failure(input, e)
        }
    }

    override suspend fun cancel(input: Data?): Boolean {
        //如果已经完成或者取消，则无需再次操作
        if (!state.can2Cancel) {
            return true
        }
        //取消时先将状态改为Cancel，再调用cancel方法，以阻止其执行
        state = State.Cancel
        return super.cancel(input)
    }
}