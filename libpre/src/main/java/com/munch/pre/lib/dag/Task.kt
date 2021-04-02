package com.munch.pre.lib.dag

import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext


/**
 * Create by munch1182 on 2021/4/1 17:28.
 */
abstract class Task {

    abstract suspend fun start(executor: Executor)

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

    internal fun run(executor: Executor) {
        if (dependsOnCopy.isNotEmpty()) {
            // TODO: 2021/4/2 有没有更协程的方式
            cd = CountDownLatch(dependsOnCopy.size)
            //不同线程但有依赖关系的任务，需要等待
            //如果是同一线程的依赖关系，dependsOnCopy应该为空
            cd?.await()
        }
        GlobalScope.launch(this@Task.coroutineContext) {
            start(executor)
            executor.notifyBeDepended(uniqueKey)
        }
    }

    internal fun countDown() {
        cd?.countDown()
    }

    internal class TaskZero : Task() {
        companion object {
            const val KEY = "com.munch.pre.lib.dag.TaskZero"
        }

        override suspend fun start(executor: Executor) {
        }

        override val uniqueKey: String = KEY
    }

    override fun toString(): String {
        return "${this::class.java.simpleName}{uniqueKey=$uniqueKey, priority=$priority, " +
                "dependsOn=${dependsOn()?.joinToString(", ") ?: "null"}}"
    }
}