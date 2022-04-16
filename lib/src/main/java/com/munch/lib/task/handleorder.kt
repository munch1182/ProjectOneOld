package com.munch.lib.task

import android.util.ArrayMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Create by munch1182 on 2022/4/16 18:48.
 */
interface ITaskOrder {

    /**
     * 用于标识该任务属于哪一条有序任务链
     */
    val orderKey: Key
}

interface OrderTask : ITask, ITaskOrder

internal class TaskOrderHandler : TaskHandler {

    private val map = ConcurrentHashMap<Int, TaskChain>()
    private val mutex = Mutex()
    private var state: State = State.Wait

    override suspend fun add(task: TaskWrapper) {
        if (task.task !is ITaskOrder) {
            throw IllegalStateException()
        }
        mutex.withLock {
            val order = task.task.orderKey.hashCode()
            val chain = map.getOrElse(order) { TaskChain(order) }
            chain.add(OrderTaskWrapper(task))
            map[order] = chain
        }
    }

    override suspend fun run() {
        if (state.isExecuting) {
            return
        }
        mutex.withLock {
            state = State.Executing
            //只是TaskChain的运行，不是实际任务执行的线程
            /*while (map.isNotEmpty()) {
                if (state.isCancel) {
                    return
                }
                *//*map.removeAt(0)?.run()*//*
                map[0]?.run()
                map.remo
            }*/
            map.values.forEach { it.run() }
            // TODO: 中途加入会出错
            map.clear()
        }
        state = State.Complete
    }

    override suspend fun cancel() {
        super.cancel()
        state = State.Cancel
        mutex.withLock {
            map.values.forEach { it.cancel() }
            map.clear()
        }
    }
}

internal class OrderTaskWrapper(val task: TaskWrapper) {
    var next: OrderTaskWrapper? = null

    /**
     * 任务依次执行
     *
     * 一个队列的任务会依次执行
     *
     * 如果取消了任务，后续任务也不会再执行
     */
    suspend fun run() {
        if (task.state.isCancel) {
            return
        }
        //切换到该线程，注意，发起的线程也会被阻塞
        withContext(TaskHelper.TaskScope.coroutineContext) {
            withContext(task.coroutines) {
                val delay = task.delayTime
                if (delay > 0) {
                    TaskHelper.log.log("delay ${delay}ms to wait ${task.key}.")
                    delay(delay)
                }
                task.run()
            }
        }
        next?.run()
    }

    /**
     * 实际取消任务，即使正在执行中，也要将状态改完取消
     */
    suspend fun cancel() {
        task.state.let {
            if (!task.state.isComplete) {
                if (task.cancel()) {
                    task.state = State.Cancel
                } else {
                    task.state = State.Complete
                }
            }
            next?.cancel()
        }
    }
}

internal class TaskChain(orderKey: Int) {

    var state: State = State.Wait
        private set
    private var head: OrderTaskWrapper? = null
    private var tail: OrderTaskWrapper? = null

    init {
        TaskHelper.log.log("$this create for order $orderKey")
    }

    fun add(task: OrderTaskWrapper): TaskChain {
        if (head == null) {
            head = task
        }
        tail?.next = task
        tail = task
        return this
    }

    suspend fun run() {
        //正在执行中，则不需要再次运行，后续添加的任务自然会执行
        if (state.isExecuting) {
            return
        }
        state = State.Executing
        TaskHelper.TaskScope.launch(Dispatchers.Default) {
            //此方法会阻塞直到所有任务完成
            head?.run()
            if (!state.isCancel) {
                state = State.Complete
            }
        }
    }

    /**
     * 取消全部任务
     * 正在执行中的任务需要自行停止
     * 未执行的任务不会再执行
     */
    suspend fun cancel() {
        head?.cancel()
        state = State.Cancel
    }
}