package com.munch.lib.helper

import com.munch.lib.BaseApp
import com.munch.lib.UNCOMPLETE
import com.munch.lib.log
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by munch1182 on 2020/12/29 21:45.
 */
object ThreadHelper {

    private const val ID_POOL_ONE_CORE_CACHED = 0
    private const val ID_POOL_ONE_CORE = 1
    private const val ID_POOL_CACHED = 2
    private const val ID_POOL_SCHEDULED = 3

    private val parentThreadGroup by lazy { object : ThreadGroup("thread-pool-group") {} }

    @UNCOMPLETE("当前实现发生错误时不会报错，但是未回调此方法")
    private class NameThreadGroup(private val id: Int) :
        ThreadGroup(parentThreadGroup, "pool-$id") {
        override fun uncaughtException(t: Thread, e: Throwable) {
            /*super.uncaughtException(t, e)*/
            //错误收集
            log(t.threadGroup?.toString(), e.message)
            e.printStackTrace()
            //测试模式下直接抛出错误
            if (BaseApp.debugMode()) {
                throw e
            }
            //非测试模式尝试忽略错误，尝试下一个任务
            (getExecutor(id) as? ThreadPoolExecutor?)?.queue?.poll()
        }
    }

    private fun newThreadGroup(id: Int) = NameThreadGroup(id)

    private val num by lazy { AtomicInteger(1) }

    private val oneCoreCachedPool by lazy {
        ThreadPoolExecutor(
            1, Int.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue(), newThreadFactory(ID_POOL_ONE_CORE_CACHED)
        )
    }

    private val oneCorePool by lazy {
        ThreadPoolExecutor(
            1, 1,
            0L, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue(),
            newThreadFactory(ID_POOL_ONE_CORE)
        )
    }

    private val cachePool by lazy {
        ThreadPoolExecutor(
            0, Int.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue(), newThreadFactory(ID_POOL_CACHED)
        )
    }

    private var scheduledPool: ScheduledExecutorService? = null

    private fun newThreadFactory(id: Int): ThreadFactory =
        ThreadFactory { Thread(newThreadGroup(id), it, "pool$id-thread-${num.getAndIncrement()}") }

    private fun getExecutor(id: Int): ExecutorService? {
        return when (id) {
            ID_POOL_CACHED -> cachePool
            ID_POOL_ONE_CORE -> oneCorePool
            ID_POOL_ONE_CORE_CACHED -> oneCoreCachedPool
            ID_POOL_SCHEDULED -> scheduledPool
            else -> null
        }
    }

    fun getExecutor(
        sizeCore: Int = 1,
        sizeNeedCache: Int = Int.MAX_VALUE,
        scheduled: Boolean = false
    ): ExecutorService {
        return when {
            sizeCore == 1 && scheduled -> getSingleScheduledPool()
            sizeCore == 0 && sizeNeedCache > 0 -> cachePool
            sizeCore >= 1 && sizeNeedCache == 0 -> oneCorePool
            else -> oneCoreCachedPool
        }
    }

    /**
     * 但其并不是线程安全的
     */
    fun getSingleScheduledPool(): ScheduledExecutorService {
        if (scheduledPool == null) {
            scheduledPool =
                Executors.newSingleThreadScheduledExecutor(newThreadFactory(ID_POOL_SCHEDULED))
        }
        return scheduledPool!!
    }

    /**
     * 终止scheduledPool中的任务
     *
     * 注意：其它的线程池不支持[ExecutorService.shutdownNow]这样的调用
     * 只有[scheduledPool]支持
     */
    fun shutdownNowScheduledThread() {
        scheduledPool?.shutdownNow()
        scheduledPool = null
    }

    fun <T> submit(
        runnable: Runnable,
        result: T,
        executor: ExecutorService = cachePool
    ): Future<T> {
        return executor.submit(runnable, result)
    }

    fun submit(runnable: Runnable, executor: ExecutorService = cachePool): Future<*> {
        return executor.submit(runnable)
    }

    fun getThreadsGroup() = parentThreadGroup

    fun interruptAll() = getThreadsGroup().interrupt()
    fun getActiveCount() = getThreadsGroup().activeCount()


}