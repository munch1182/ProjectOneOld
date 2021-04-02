package com.munch.pre.lib.dag

import android.util.ArrayMap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Create by munch1182 on 2021/4/1 17:28.
 */
class Executor {

    private val dag = Dag<String>()
    private val taskMap = ArrayMap<String, Task>()
    private val dependMap = ArrayMap<String, MutableList<String>>()

    /**
     * 执行器所在线程，需要保证单线程
     */
    private val executorDispatcher = CoroutineName("executor") + Dispatchers.Default
    private val executeDispatcher = CoroutineName("execute") + Dispatchers.IO


    fun add(task: Task): Executor {
        GlobalScope.launch(executorDispatcher) {
            taskMap[task.uniqueKey] = task
            task.copyDepends().forEach { key ->
                if (dependMap.contains(key)) {
                    dependMap[key]!!.add(task.uniqueKey)
                } else {
                    dependMap[key] = mutableListOf(task.uniqueKey)
                }
                dag.addEdge(
                    Dag.Edge(
                        Dag.Point(key, replaceStrategy = Dag.REPLACE_HIGHER_PRIORITY),
                        Dag.Point(task.uniqueKey, task.priority, Dag.REPLACE_HIGHER_PRIORITY)
                    )
                )
            }
        }
        return this
    }

    fun execute() {
        GlobalScope.launch(executorDispatcher) {
            taskMap[Task.TaskZero.KEY] = Task.TaskZero()
            dag.generaDag()
                .map { taskMap[it.key]!! }
                .asFlow()
                .map { task ->
                    launch(executeDispatcher) { task.run(this@Executor) }
                    task.uniqueKey
                }.catch { e ->
                    e.printStackTrace()
                    emit("")
                }.collect {
                }
        }
    }

    internal fun notifyBeDepended(uniqueKey: String) {
        dependMap[uniqueKey]?.forEach {
            val task = taskMap[it] ?: return@forEach
            if (task.cd != null) {
                task.countDown()
            } else {
                task.dependsOnCopy.remove(uniqueKey)
            }
        }
    }

    fun getTaskByKey(key: String): Task? {
        return taskMap[key]
    }


}