package com.munch.pre.lib.dag

import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext

/**
 *
 * Create by munch1182 on 2021/4/1 17:28.
 */
abstract class Task : CoroutineScope {

    abstract fun start(executor: Executor)

    abstract val uniqueKey: String

    open fun dependsOn(): MutableList<String>? = null

    /**
     * 该任务在同时执行的任务中的优先级
     */
    open val priority: Int = 0

    /**
     * 指定任务执行的线程
     */
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined

    /**
     * 当前任务需要进行等待
     * 当前任务以及依赖此任务的任务都会进行等待，直到[next]被调用
     *
     * @see next
     */
    fun await() = runBlocking(coroutineContext) { await = true }
    fun next() = runBlocking(coroutineContext) {
        if (!await) {
            return@runBlocking
        }
        await = false
        next?.invoke()
    }

    //<editor-fold desc="">
    private var next: (() -> Unit)? = null
    private var await = false
    internal var cd: CountDownLatch? = null
    internal var dependsOnCopy = mutableListOf<String>()

    internal fun copyDepends(): MutableList<String> {
        if (uniqueKey == TaskZero.KEY) {
            return dependsOnCopy
        }
        val dependsOnOrigin = dependsOn()
        if (dependsOnOrigin.isNullOrEmpty()) {
            dependsOnCopy.add(TaskZero.KEY)
        } else {
            dependsOnCopy.clear()
            dependsOnCopy.addAll(dependsOnOrigin)
        }
        return dependsOnCopy
    }

    /**
     * 此方法在[Executor.executeDispatcher]中执行
     * 而[start]方法才在指定线程中执行
     */
    internal fun run(executor: Executor) {
        //在Executor线程中执行此代码
        if (dependsOnCopy.isNotEmpty()) {
            cd = CountDownLatch(dependsOnCopy.size)
            //需要等待依赖任务完成
            cd?.await()
        }
        cd = null
        executor.launch(this@Task.coroutineContext) {
            try {
                start(executor)
                if (await) {
                    next = {
                        executor.executeCallBack.invoke(uniqueKey, executor)
                        executor.notifyBeDepended(uniqueKey)
                    }
                    return@launch
                }
                executor.executeCallBack.invoke(uniqueKey, executor)
            } catch (e: Exception) {
                executor.exceptionListener?.invoke(e)
            }
            executor.notifyBeDepended(uniqueKey)
        }
    }

    internal fun countDown() {
        cd?.countDown()
    }

    /**
     * 第一个任务
     */
    internal class TaskZero : Task() {
        companion object {
            const val KEY = "com.munch.pre.lib.dag.TaskZero"
        }

        override fun start(executor: Executor) {
        }

        override val uniqueKey: String = KEY

    }

    /**
     * 最后一个任务
     */
    internal class TaskOne(private val dependOn: MutableList<String>) : Task() {
        companion object {
            const val KEY = "com.munch.pre.lib.dag.TaskOne"
        }

        override fun start(executor: Executor) {
        }

        override fun dependsOn(): MutableList<String> = dependOn

        override val uniqueKey: String = KEY
    }
    //</editor-fold>

    override fun toString(): String {
        return "${this::class.java.simpleName}{uniqueKey=$uniqueKey, priority=$priority, " +
                "dependsOn=${dependsOn()?.joinToString(", ", "[", "]") ?: "null"}}"
    }
}