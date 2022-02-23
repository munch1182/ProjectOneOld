package com.munch.lib.task

import androidx.annotation.IntDef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/2/23 15:18.
 */
open class TaskType(
    val type: String?,
    //此类型的任务是否只能单独执行，不能并行
    val isOnly: Boolean = false
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        type ?: return false
        if (javaClass != other?.javaClass) return false
        other as TaskType
        other.type ?: return false
        if (type != other.type) return false
        return true
    }

    override fun hashCode(): Int {
        return type?.hashCode() ?: 0
    }
}

@IntDef(RepeatPolicy.IGNORE, RepeatPolicy.REPLACE)
@Retention(AnnotationRetention.SOURCE)
annotation class RepeatPolicy {

    companion object {

        const val REPLACE = 0
        const val IGNORE = 1
    }
}

open class TaskKey(
    val key: String,
    @RepeatPolicy
    val repeatPolicy: Int = RepeatPolicy.REPLACE,
    val type: TaskType? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TaskKey) return false
        if (key != other.key) return false
        if (type != other.type) return false
        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return key
    }


}

interface ITask<I, O> {

    val key: TaskKey

    /**
     * 此任务执行的上下文
     */
    val thread: CoroutineContext
        get() = Dispatchers.Main

    /**
     * 此任务执行的回调
     */
    fun run(input: I?): O?

    /**
     * 此任务所依赖的前置任务
     */
    fun getDependsTask(): List<TaskKey>? = null
}

interface SimpleTask : ITask<Unit, Unit> {

    override fun run(input: Unit?): Unit? {
        run()
        return null
    }

    fun run()
}

internal class TaskWrapper(val task: ITask<Any, Any>) {

    //依赖此任务的任务
    internal var beDependent: MutableList<TaskKey>? = null

    //此任务依赖的任务
    internal var depended: MutableList<TaskKey>? = null
    private var input: Any? = null
    private var out: Any? = null

    suspend fun start(helper: TaskHelper) {
        withContext(task.thread) {
            out = task.run(input)
            beDependent?.forEach { key ->
                val w = helper.taskMap[key]
                w?.input = out
                w?.start(helper)
            }
        }
    }

    fun canStart(): Boolean {
        return (depended?.size ?: 0) == 0
    }
}

class TaskHelper {

    //将task的list转为Map
    internal val taskMap = hashMapOf<TaskKey, TaskWrapper>()
    private var onSorted: OnSortedListener? = null

    @Suppress("UNCHECKED_CAST")
    fun <I, O> add(task: ITask<I, O>): TaskHelper {

        val wrapper = TaskWrapper(task as ITask<Any, Any>)
        val key = wrapper.task.key
        if (taskMap.containsKey(key)) {
            when (key.repeatPolicy) {
                RepeatPolicy.IGNORE -> return this
                RepeatPolicy.REPLACE -> {
                    //nothing
                }
            }
        }
        taskMap[key] = wrapper
        return this
    }

    fun start() {
        val sortedList = sortTask()

        onSorted?.onSorted(sortedList)

        runBlocking(Dispatchers.IO) {
            //执行所有无依赖的任务
            //主要是为了避免不需要当前执行的任务占用线程
            sortedList.forEach {
                val wrapper = taskMap[it] ?: return@forEach
                if (wrapper.canStart()) {
                    wrapper.start(this@TaskHelper)
                }
            }
        }
    }

    fun onSorted(onSorted: OnSortedListener): TaskHelper {
        this.onSorted = onSorted
        return this
    }

    private fun sortTask(): MutableList<TaskKey> {

        //依赖该key任务的所有任务
        val taskDependMap = hashMapOf<TaskKey, MutableList<TaskKey>>()
        //无依赖的任务队列
        val list = ArrayDeque<TaskKey>()
        //该任务的依赖数目
        val taskCountMap = hashMapOf<TaskKey, Int>()

        taskMap.values.forEach {
            val key = it.task.key

            if (taskCountMap.containsKey(key)) {
                throw IllegalArgumentException("tag repeat: $key")
            }

            val depends = it.task.getDependsTask()?.toMutableList() ?: mutableListOf()

            taskCountMap[key] = depends.size
            it.depended = depends

            depends.forEach { k ->
                val data = taskDependMap[k] ?: mutableListOf()
                data.add(key)
                taskDependMap[k] = data
            }

            if (depends.size == 0) {
                list.offer(key)
            }
        }

        if (list.isEmpty()) {
            throw IllegalArgumentException("circular dependencies")
        }


        val result = mutableListOf<TaskKey>()
        while (list.isNotEmpty()) {
            val key = list.pop()

            result.add(key)
            val dependList = taskDependMap[key]
            taskMap[key]?.beDependent = dependList

            dependList?.forEach { k ->

                var count = taskCountMap[k] ?: 0
                count--
                taskCountMap[k] = count
                if (count == 0) {
                    list.offer(k)
                }
            }

        }

        return result
    }

    interface OnSortedListener {

        fun onSorted(list: MutableList<TaskKey>)
    }
}