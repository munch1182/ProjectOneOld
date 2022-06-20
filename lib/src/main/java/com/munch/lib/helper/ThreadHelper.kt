package com.munch.lib.helper

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min

/**
 * Created by munch1182 on 2022/5/8 16:12.
 */
object ThreadHelper {
    //<editor-fold desc="pool">
    //cpu数量
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    //线程池最大容纳线程数
    private val MAX_THREAD_SIZE = CPU_COUNT * 2 + 1

    //最大核心线程数量
    private val MAX_CORE_NUM = max(1, min(CPU_COUNT - 1, 4))

    private val poolNum = AtomicInteger()
    private val threadNum = AtomicInteger()
    private val nameThreadGroup by lazy { NameThreadGroup() }
    private var handler: Thread.UncaughtExceptionHandler? = null

    class NameThread(private val name: String) : ThreadFactory {

        override fun newThread(runnable: Runnable?): Thread {
            return Thread(nameThreadGroup, runnable, "p-$name t-${threadNum.getAndIncrement()}")
        }
    }

    class NameThreadGroup : ThreadGroup("pool-${poolNum.getAndIncrement()}") {

        /**
         * 注意：submit的任务不会触发uncaughtException，因为其实现内部调用了try..catch拦截了异常
         */
        override fun uncaughtException(t: Thread, e: Throwable) {
            e.printStackTrace()
            handler?.uncaughtException(t, e) ?: super.uncaughtException(t, e)
        }
    }

    /**
     * 作为公用的线程池，不能被shutdown
     */
    private val cachedPool by lazy {
        ThreadPoolExecutor(
            0, MAX_THREAD_SIZE, 10, TimeUnit.SECONDS,
            SynchronousQueue(), NameThread("cached-app"), ThreadPoolExecutor.AbortPolicy()
        )
    }

    /**
     * @return 一个新建的固定线程池，根据业务需求自行保存对象
     */
    fun newFixPool(coreNum: Int = 1): ThreadPoolExecutor {
        if (coreNum < MAX_CORE_NUM) {
            throw IllegalStateException()
        }
        return ThreadPoolExecutor(
            coreNum, coreNum, 0, TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            NameThread("fix"),
            ThreadPoolExecutor.AbortPolicy()
        )
    }

    fun newCachePool() = ThreadPoolExecutor(
        0, MAX_THREAD_SIZE, 10, TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        NameThread("cached"),
        ThreadPoolExecutor.AbortPolicy()
    )

    /**
     * @return 一个新建的周期性线程池，根据业务需求自行保存对象
     */
    fun newScheduledPool(coreNum: Int = 1) =
        Executors.newScheduledThreadPool(
            coreNum, NameThread("schedule")
        )

    fun execute(task: Runnable) = cachedPool.execute(task)
    fun remove(task: Runnable) = cachedPool.remove(task)

    fun setExceptionHandler(h: Thread.UncaughtExceptionHandler?) {
        handler = h
    }

    fun pool(submit: Boolean = false, block: () -> Unit) {
        if (submit) {
            cachedPool.submit { block.invoke() }
        } else {
            cachedPool.execute { block.invoke() }
        }
    }

    fun pool(submit: Boolean = false, task: Runnable) {
        if (submit) {
            cachedPool.submit(task)
        } else {
            cachedPool.execute(task)
        }
    }

    fun <T> pool(task: Callable<T>): Future<T>? = cachedPool.submit(task)
    //</editor-fold>

    val mainHandler by lazy { Handler(Looper.getMainLooper()) }
}