package com.munch.lib.dag

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resumeWithException

/**
 * Create by munch1182 on 2021/2/25 16:30.
 */
abstract class Task {

    private var cancellable: CancellableContinuation<Boolean>? = null

    private var executeBlock = false

    abstract fun start(executor: Executor)


    /**
     * 指定此任务的执行线程
     *
     * 注意：任务执行使用的是flow，且任务会依次执行，所以线程耗时即任务耗时
     */
    open val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    open fun getPriority(): Int = 0

    abstract val uniqueKey: Key

    open fun dependsOn(): MutableList<Key> = mutableListOf()

    internal open fun run(executor: Executor): Flow<Boolean> {
        return flow<Boolean> {
            emit(suspendCancellableCoroutine {
                this@Task.cancellable = it
                try {
                    start(executor)
                } catch (e: Exception) {
                    it.resumeWithException(e)
                }
                if (!executeBlock) {
                    next()
                }
            })
        }.flowOn(dispatcher)
            .onEach {
                cancellable = null
                executor.executeCallBack?.invoke(this, executor)
            }.flowOn(dispatcher)
            .catch { e -> executor.errorCallBack?.invoke(this@Task, e, executor) }
    }

    /**
     * 声明此任务是阻塞式的，需要自行处理下一个任务的时机
     * 调用此方法后将不会在[start]方法执行完后执行下一个任务，直到[next]被调用
     */
    protected fun signBlock() {
        executeBlock = true
    }

    /**
     * 如果调用[signBlock]，则只有调用此方法时才会继续下一个任务
     * 否则将会一致阻塞在调用[signBlock]处
     */
    protected fun next() {
        cancellable?.resumeWith(Result.success(true))
    }
}

data class Key(val uniqueKey: String) {

    override fun hashCode(): Int {
        return uniqueKey.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is Key) {
            return uniqueKey == other.uniqueKey
        }
        return false
    }
}
