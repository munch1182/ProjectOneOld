package com.munch.pre.lib.helper

import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.log.log
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 缓存了线程池对象
 *
 * Create by munch1182 on 2021/4/6 11:22.
 */
object ThreadPoolHelper {

    private const val ID_CACHE_IO = 1
    private const val ID_CACHE_CPU = 4
    private const val ID_SCHEDULED = 2
    private const val ID_FIX = 3
    private const val ID_NEW_IO = 5
    private val fixId = AtomicInteger(1)

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val CORE_POOL_SIZE = 1.coerceAtLeast((CPU_COUNT - 1).coerceAtMost(4))

    val CACHE_IO by lazy {
        ThreadPoolExecutor(
            0, 50, 3L, TimeUnit.SECONDS, SynchronousQueue(),
            newThreadFactory(ID_CACHE_IO)
        )
    }

    val CACHE_CPU by lazy {
        ThreadPoolExecutor(
            CORE_POOL_SIZE, 50, 30L, TimeUnit.SECONDS, SynchronousQueue(),
            newThreadFactory(ID_CACHE_CPU)
        )
    }

    val SCHEDULED by lazy { ScheduledThreadPoolExecutor(1, newThreadFactory(ID_SCHEDULED)) }

    /**
     * 根据业务需求自行保存对象
     */
    fun newFixThread(coreSize: Int = 1, name: String? = null): ThreadPoolExecutor {
        return ThreadPoolExecutor(
            coreSize, coreSize,
            0L, TimeUnit.SECONDS,
            LinkedBlockingQueue(), newThreadFactory(ID_FIX * 10 + fixId.getAndIncrement(), name)
        )
    }

    fun newCachePool(coreSize: Int, maxSize: Int, aliveTime: Long, name: String? = null) =
        ThreadPoolExecutor(
            coreSize, maxSize, aliveTime, TimeUnit.MILLISECONDS, SynchronousQueue(),
            newThreadFactory(ID_NEW_IO, name)
        )

    private val num by lazy { AtomicInteger(1) }
    private fun newThreadFactory(
        id: Int, name: String? = null
    ): ThreadFactory =
        ThreadFactory {
            Thread(newThreadGroup(id), it, name ?: "pool$id-thread-${num.getAndIncrement()}")
        }

    private val parentGroup by lazy { object : ThreadGroup("thread-pool-group") {} }
    private fun newThreadGroup(id: Int) = NameThreadGroup(id)

    private class NameThreadGroup(private val id: Int) : ThreadGroup(parentGroup, "pool-$id") {

        /**
         * 此异常处理只会被[ThreadPoolExecutor.execute]回调
         */
        override fun uncaughtException(t: Thread, e: Throwable) {
            log(e)
            e.printStackTrace()
            //测试模式下不处理错误
            if (BaseApp.debug()) {
                super.uncaughtException(t, e)
            }
            //非测试模式下，若是SCHEDULED则尝试执行下一个任务，否则无视异常
            if (id == ID_SCHEDULED) {
                SCHEDULED.queue.poll()
            }
        }
    }

}