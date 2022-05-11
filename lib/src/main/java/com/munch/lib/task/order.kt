package com.munch.lib.task

import com.munch.lib.Destroyable
import com.munch.lib.log.Logger
import kotlinx.coroutines.*

/**
 * Created by munch1182 on 2022/5/11 23:34.
 */
class OrderTaskHelper(
    override val key: Key,
    private val log: Logger = Logger("OrderTask")
) : ITask, Destroyable {

    private var head: OrderTaskWrapper? = null
    private var tail: OrderTaskWrapper? = null

    private var input: Data? = null
    private var state: State = State.Wait
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

    override suspend fun run(input: Data?): Result {
        if (!scope.isActive) {
            return Result.Failure()
        }
        if (!state.needRun) {
            return Result.Invalid
        }
        scope.launch(Dispatchers.Default) {

            executeTaskImp()

            if (!state.isCancel) {
                state = State.Complete
            }
        }

        return Result.Invalid
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