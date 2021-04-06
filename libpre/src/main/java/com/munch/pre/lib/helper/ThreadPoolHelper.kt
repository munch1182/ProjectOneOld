package com.munch.pre.lib.helper

import com.munch.pre.lib.base.BaseApp
import com.munch.pre.lib.extend.log
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * 缓存了线程池对象
 *
 * Create by munch1182 on 2021/4/6 11:22.
 */
object ThreadPoolHelper {

    private const val ID_CACHE = 1
    private const val ID_SCHEDULED = 2
    private const val ID_FIX = 3

    val CACHE by lazy {
        ThreadPoolExecutor(
            0, 50, 30L, TimeUnit.SECONDS, SynchronousQueue(),
            newThreadFactory(ID_CACHE)
        )
    }

    val SCHEDULED by lazy { ScheduledThreadPoolExecutor(1, newThreadFactory(ID_SCHEDULED)) }

    /**
     * 根据业务需求自行保存对象
     */
    fun newFixThread(coreSize: Int = 1): ThreadPoolExecutor {
        return ThreadPoolExecutor(
            coreSize, coreSize,
            0L, TimeUnit.SECONDS,
            LinkedBlockingQueue(), newThreadFactory(ID_FIX)
        )
    }

    private val num by lazy { AtomicInteger(1) }
    private fun newThreadFactory(id: Int): ThreadFactory =
        ThreadFactory {
            Thread(newThreadGroup(id), it, "pool$id-thread-${num.getAndIncrement()}")
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