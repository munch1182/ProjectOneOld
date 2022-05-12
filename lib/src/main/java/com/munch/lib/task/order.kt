package com.munch.lib.task

import com.munch.lib.Destroyable
import com.munch.lib.log.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Created by munch1182 on 2022/5/11 23:34.
 */
class OrderTaskHelper(
    private val log: Logger = Logger("OrderTask")
) : Destroyable {

    private var head: OrderTaskWrapper? = null
    private var tail: OrderTaskWrapper? = null

    private var input: Data? = null
    private val lock = Mutex()
    private var state: State = State.Wait
        set(value) {
            runBlocking { lock.withLock { field = value } }
        }
        get() = runBlocking { lock.withLock { field } }

    private val scope = ContextScope(SupervisorJob() + CoroutineName("OrderTask"))

    fun add(task: ITask): OrderTaskHelper {
        val key = task.key
        val wrapper = OrderTaskWrapper(key, task, log)
        if (head == null) {
            head = wrapper
        }
        tail?.next = wrapper
        tail = wrapper
        return this
    }

    /**
     * 传入初始参数
     */
    fun input(input: Data?): OrderTaskHelper {
        this.input = input
        return this
    }

    suspend fun run() {
        if (!scope.isActive) {
            return
        }
        if (!state.needRun) {
            return
        }
        scope.launch(Dispatchers.Default) {

            executeTaskImp()

            if (!state.isCancel) {
                state = State.Complete
            }
        }

    }

    private suspend fun executeTaskImp() {
        var now = head
        while (now != null) {
            //如果此时发现任务已经取消
            if (state.isCancel) {
                return
            }

            state = State.Executing

            //调用执行方法，并获取结果
            var result = now.run(input)
            //传递参数
            input = result.data
            //重试机制
            while (result.needRetry) {
                result = now.run(input)
            }
            //下一个任务
            now = now.next
        }
    }

    suspend fun cancel() {

        state = State.Cancel

        scope.launch(Dispatchers.Default) {
            //依次执行取消方法
            var now = head
            while (now != null) {
                now.cancel(input)
                now = now.next
            }
        }
    }

    override fun destroy() {
        scope.cancel()
    }

}

class OrderTaskWrapper(key: Key, task: ITask, log: Logger) : TaskWrapper(key, task, log) {

    var next: OrderTaskWrapper? = null
}