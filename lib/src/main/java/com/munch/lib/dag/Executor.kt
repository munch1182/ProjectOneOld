package com.munch.lib.dag

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * Create by munch1182 on 2021/2/25 16:31.
 */
class Executor private constructor() {

    companion object {
        private val INSTANCE by lazy { Executor() }

        fun getInstance() = INSTANCE

        fun Dag<Key>.addTask(task: Task) {
            val depends = task.dependsOn()
            val point = Dag.Point(task.uniqueKey, inDegree = 0)
            if (depends.isEmpty()) {
                addEdge(Dag.Edge.point2Edge(point))
            } else {
                depends.forEach {
                    val from = Dag.Point(it, inDegree = 0)
                    this.addEdge(Dag.Edge(from = from, to = point))
                }
            }
        }
    }

    private val taskDependMap = mutableMapOf<Key, Task>()
    private var finishCallBack: ((executor: Executor) -> Unit)? = null
    private var executeCallBack: ((task: Task, executor: Executor) -> Unit)? = null
    private var errorCallBack: ((task: Task, e: Exception, executor: Executor) -> Unit)? = null
    private val dag: Dag<Key> = Dag()

    fun add(task: Task): Executor {
        taskDependMap[task.uniqueKey] = task
        return this
    }

    fun execute() {
        taskDependMap.values.sortedBy { it.getPriority() }.forEach {
            dag.addTask(it)
        }
        dumpTask()
    }

    fun executeCallBack(callBack: (task: Task, executor: Executor) -> Unit): Executor {
        this.executeCallBack = callBack
        return this
    }

    fun errorCallBack(errorCallBack: (task: Task, exception: Exception, executor: Executor) -> Unit): Executor {
        this.errorCallBack = errorCallBack
        return this
    }

    private fun dumpTask() {
        val executor = this@Executor
        runBlocking {
            dag.dump()
                .map { taskDependMap[it.point] ?: throw IllegalStateException("cannot find task") }
                .asFlow()
                .collect {
                    try {
                        flowOf(it.start(executor))
                            .flowOn(it.dispatcher)
                            .collect()
                    } catch (e: Exception) {
                        errorCallBack?.invoke(it, e, executor)
                    }
                }
        }
        finishCallBack?.invoke(executor)
    }
}