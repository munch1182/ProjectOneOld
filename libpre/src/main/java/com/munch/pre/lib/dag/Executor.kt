package com.munch.pre.lib.dag

import android.util.ArrayMap
import com.munch.pre.lib.extend.log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * 用于有依赖关系的系列任务执行
 * 基于有向无环图
 *
 * 目前只能执行在执行之前已经确定了依赖关系的任务，不能动态添加任务
 * 目前的实现方法是将协程作为切换线程的工具，实际上使用阻塞线程的方式来实现依赖的先后执行
 *
 * Create by munch1182 on 2021/4/1 17:28.
 */
class Executor : CoroutineScope {

    private val job = Job()
    private val dag = Dag<String>()
    private val taskMap = ArrayMap<String, Task>()
    private val dependMap = ArrayMap<String, MutableList<String>>()
    private var executeListener: ((key: String, executor: Executor) -> Unit)? = null
    private var executedListener: ((executor: Executor) -> Unit)? = null
    internal val executeCallBack: (key: String, executor: Executor) -> Unit = { key, executor ->
        executeListener?.invoke(key, executor)
        if (key == Task.TaskOne.KEY) {
            executing = false
            executedListener?.invoke(executor)
        }
    }

    internal var exceptionListener: ((e: Exception) -> Unit)? = null

    /**
     * 执行器所在线程，需要保证线程安全
     */
    override val coroutineContext = CoroutineName("executor") + ExecutorCoroutineDispatcher + job

    /**
     * 执行部分所在线程，不干扰执行器线程
     */
    private val executeDispatcher = CoroutineName("execute") + Dispatchers.IO + job
    private var executing = false

    /**
     * 此类是单例的保证线程池复用，因此需要手动关闭
     */
    internal object ExecutorCoroutineDispatcher : CoroutineDispatcher() {

        private var executorThread: ExecutorService? = null

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (executorThread == null) {
                executorThread = Executors.newSingleThreadExecutor()
            }
            executorThread?.execute(block)
        }

        fun release() {
            executorThread?.shutdown()
            executorThread = null
        }
    }

    fun cancel() {
        job.cancel()
    }

    fun add(task: Task): Executor {
        launch {
            if (executing) {
                throw IllegalStateException("cannot add task when executor is executing")
            }
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

    fun execute(): Executor {

        launch {
            log(job.isCancelled)
            addDefTask()
            executing = true
            dag.generaDag()
                .map { taskMap[it]!! }
                .asFlow()
                .collect { task ->
                    //可以并发执行，但有依赖的任务仍需要等待依赖任务先完成
                    //执行的逻辑是线性的且有依赖关系，因为并发而提高了效率，因此要使用线程的等待机制
                    launch(executeDispatcher) { task.run(this@Executor) }
                }
        }
        return this
    }

    fun clear() {
        dag.clear()
        taskMap.clear()
        dependMap.clear()
    }

    private suspend fun addDefTask() {
        executing = true
        //添加t0
        taskMap[Task.TaskZero.KEY] = Task.TaskZero()
        //添加t1
        val dependOn = mutableListOf<String>()
        taskMap.keys.forEach {
            if (!dependMap.containsKey(it)) {
                dependOn.add(it)
            }
        }
        executing = false
        add(Task.TaskOne(dependOn))
        //等待添加完成
        yield()
    }

    /**
     * 添加每个任务的执行回调
     *
     * 任务完成是根据[Task.start]方法是否执行完毕来判断的
     */
    fun setExecuteListener(func: ((key: String, executor: Executor) -> Unit)? = null): Executor {
        launch { executeListener = func }
        return this
    }

    /**
     * 回调执行异常
     *
     * 当异常发送时会回调此方法
     *
     * 注意：异常并不会阻碍任务流程顺序
     *
     */
    fun setExceptionListener(func: ((e: Exception) -> Unit)? = null): Executor {
        launch { exceptionListener = func }
        return this
    }

    /**
     * 执行完所有任务的回调
     */
    fun setExecutedListener(func: ((executor: Executor) -> Unit)? = null): Executor {
        launch { executedListener = func }
        return this
    }

    /**
     * 主要用于关闭线程池
     *
     * 调用此方法后，此类将不可用，执行将报错，因此只能在最后退出时使用
     *
     */
    fun release() {
        launch { ExecutorCoroutineDispatcher.release() }
    }

    internal fun notifyBeDepended(uniqueKey: String) {
        launch {
            dependMap[uniqueKey]?.forEach {
                val task = taskMap[it] ?: return@forEach
                if (task.cd != null) {
                    task.countDown()
                } else {
                    task.dependsOnCopy.remove(uniqueKey)
                }
            }
        }
    }

    fun getTaskByKey(key: String): Task? {
        return runBlocking { withContext(coroutineContext) { taskMap[key] } }
    }

}