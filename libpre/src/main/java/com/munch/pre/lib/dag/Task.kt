package com.munch.pre.lib.dag

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext

/**
 *
 * Create by munch1182 on 2021/4/1 17:28.
 */
abstract class Task {

    abstract fun start(executor: Executor)

    abstract val uniqueKey: String

    open fun dependsOn(): MutableList<String>? = null

    open val priority: Int = 0

    /**
     * 指定任务执行的线程
     */
    open val coroutineContext: CoroutineContext = Dispatchers.Unconfined

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
        GlobalScope.launch(this@Task.coroutineContext) {
            try {
                start(executor)
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

    override fun toString(): String {
        return "${this::class.java.simpleName}{uniqueKey=$uniqueKey, priority=$priority, " +
                "dependsOn=${dependsOn()?.joinToString(", ", "[", "]") ?: "null"}}"
    }
}