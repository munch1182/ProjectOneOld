package com.munch.lib.task

import com.munch.lib.helper.data.DataFun
import com.munch.lib.log.Logger
import kotlinx.coroutines.Dispatchers
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

    /**
     * 此任务执行的上下文
     */
    val coroutines: CoroutineContext
        get() = Dispatchers.Default

    suspend fun run(input: Data?): Result

    /**
     * 当调用取消时此任务未执行完毕，则会被回调此方法
     * 需要在此处进行取消任务并返回取消结果
     */
    suspend fun cancel(input: Data?): Boolean = true
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

    override fun toString() = key.toString()
}

sealed class Result {

    class Success(private val input: Data? = null) : Result() {
        override fun toString() = "SUCCESS"
    }

    object Failure : Result() {
        override fun toString() = "FAILURE"
    }

    object Retry : Result() {
        override fun toString() = "RETRY"
    }
}

class Data(hashMap: HashMap<String, Any?>? = null) : DataFun<String> {

    constructor(data: Data) : this(HashMap(data.map))

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

}

internal open class TaskWrapper(
    override val key: Key,
    val task: ITask,
    val log: Logger
) : ITask by task {

    var state: State = State.Wait
        set(value) {
            val old = field
            field = value
            log.log { "task $key state: $old -> $field" }
        }

    override suspend fun run(input: Data?): Result {
        if (state.isCancel) {
            return Result.Failure
        }
        log.log { "task $key run." }
        return task.run(input)
    }

    override suspend fun cancel(input: Data?): Boolean {
        state = State.Cancel
        return super.cancel(input)
    }
}
