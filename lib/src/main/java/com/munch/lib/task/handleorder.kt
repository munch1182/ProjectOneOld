package com.munch.lib.task

import com.munch.lib.log.log
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
     *
     * 可能被反复调用
     */
    val orderKey: Key
}

abstract class OrderTask : Task(), ITaskOrder


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
            map.values.forEach {
                it.addCompleteTaskIfNeed(CompleteOrderTask(it.orderKey) {
                    map.remove(it.orderKey)
                    if (map.isEmpty()) {
                        state = State.Complete
                    }
                }).run()
            }
        }
    }

    override suspend fun pause() {
        super.pause()
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

internal class CompleteOrderTask(
    order: Int,
    private val onComplete: suspend () -> Unit
) : OrderTask() {

    override suspend fun run() {
        onComplete.invoke()
    }

    override val key: Key = TaskHelper.KEY_COMPLETE

    override val orderKey: Key = Key(order)

}


internal class OrderTaskWrapper(val task: TaskWrapper) {

    var next: OrderTaskWrapper? = null
    var last: OrderTaskWrapper? = null

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

internal class TaskChain(val orderKey: Int) {

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
            log("set head: ${task.task.key}")
        }
        while (tail != null && tail!!.task.task is CompleteOrderTask) {
            tail = tail!!.last
            log("set tail: ${tail?.task?.key}")
        }
        tail?.next = task
        tail?.let {
            log("set tail(${tail?.task?.key}) next: ${task.task.key}")
        }
        tail?.let { task.last = it }
        log("set tail: ${task.task.key}")
        tail = task
        return this
    }


    fun addCompleteTaskIfNeed(task: CompleteOrderTask): TaskChain {
        add(OrderTaskWrapper(TaskWrapper(task.key, task)))
        return this
    }

    suspend fun run() {
        //正在执行中，则不需要再次运行，后续添加的任务自然会执行
        if (state.isExecuting) {
            TaskHelper.log.log("call run but executing")
            return
        }
        state = State.Executing
        TaskHelper.TaskScope.launch(Dispatchers.Default) {
            //此方法会阻塞直到所有任务完成
            head?.run()
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