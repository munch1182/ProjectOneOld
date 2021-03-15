package com.munch.lib.dag

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Create by munch1182 on 2021/2/25 16:31.
 */
class Executor {

    companion object {

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

    internal var completeCallBack: ((executor: Executor) -> Unit)? = null
    internal var startCallBack: ((executor: Executor) -> Unit)? = null
    internal var executeCallBack: ((task: Task, executor: Executor) -> Unit)? = null
    internal var errorCallBack: ((task: Task, e: Throwable, executor: Executor) -> Unit)? = null

    private val taskDependMap = mutableMapOf<Key, Task>()
    fun getTasks() = taskDependMap
    fun getTask(key: String) = taskDependMap[Key(key)]
    private val dag: Dag<Key> = Dag()

    fun add(task: Task): Executor {
        taskDependMap[task.uniqueKey] = task
        return this
    }

    fun execute() {
        val executor = this
        GlobalScope.launch(Dispatchers.IO) {
            taskDependMap.values.sortedBy { -it.getPriority() }.forEach { dag.addTask(it) }
            startCallBack?.invoke(this@Executor)
            dumpTask().forEach { task -> task.run(executor).collect {} }
            completeCallBack?.invoke(this@Executor)
        }
    }

    private fun dumpTask(): List<Task> {
        return dag.dump()
            .map { taskDependMap[it.point] ?: throw IllegalStateException("cannot find task") }
    }

    fun setStartListener(listener: ((executor: Executor) -> Unit)? = null): Executor {
        startCallBack = listener
        return this
    }

    fun setCompleteListener(listener: ((executor: Executor) -> Unit)? = null): Executor {
        completeCallBack = listener
        return this
    }

    fun setErrorListener(listener: ((task: Task, e: Throwable, executor: Executor) -> Unit)?): Executor {
        errorCallBack = listener
        return this
    }

    fun setExecuteListener(listener: ((task: Task, executor: Executor) -> Unit)?): Executor {
        executeCallBack = listener
        return this
    }
}