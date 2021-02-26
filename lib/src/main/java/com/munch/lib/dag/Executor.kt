package com.munch.lib.dag

import kotlinx.coroutines.Dispatchers
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
    fun getTasks() = taskDependMap
    fun getTask(key: Key) = taskDependMap[key]
    private var finishCallBack: ((executor: Executor) -> Unit)? = null
    private var executeCallBack: MutableList<((task: Task, executor: Executor) -> Unit)?> =
        mutableListOf()
    private var errorCallBack: MutableList<((task: Task, e: Exception, executor: Executor) -> Unit)?> =
        mutableListOf()
    private val dag: Dag<Key> = Dag()

    fun add(task: Task): Executor {
        taskDependMap[task.uniqueKey] = task
        return this
    }

    fun execute() {
        taskDependMap.values.sortedBy { it.getPriority() }.forEach { dag.addTask(it) }
        val executor = this
        dumpTask()
            .forEach { task ->
                runBlocking(task.dispatcher) {
                    try {
                        flowOf(task)
                            .map { it.start(executor) }
                            .collect {
                                executeCallBack.forEach { callBack ->
                                    callBack?.invoke(task, executor)
                                }
                            }
                    } catch (e: Exception) {
                        errorCallBack.forEach { callBack ->
                            callBack?.invoke(task, e, executor)
                        }
                    }
                }
            }
        finishCallBack?.invoke(executor)
    }

    fun executeCallBack(callBack: (task: Task, executor: Executor) -> Unit): Executor {
        this.executeCallBack.add(callBack)
        return this
    }

    fun removeCallBack(callBack: (task: Task, executor: Executor) -> Unit): Executor {
        this.executeCallBack.remove(callBack)
        return this
    }

    fun removeCallBack(errorCallBack: (task: Task, exception: Exception, executor: Executor) -> Unit): Executor {
        this.errorCallBack.remove(errorCallBack)
        return this
    }

    fun errorCallBack(errorCallBack: (task: Task, exception: Exception, executor: Executor) -> Unit): Executor {
        this.errorCallBack.add(errorCallBack)
        return this
    }

    fun setFinishCallBack(finishCallBack: (executor: Executor) -> Unit): Executor {
        this.finishCallBack = finishCallBack
        return this
    }

    private fun dumpTask(): List<Task> {
        return dag.dump()
            .map { taskDependMap[it.point] ?: throw IllegalStateException("cannot find task") }
    }
}