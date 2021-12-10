package com.munch.lib.task

import android.os.Looper
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min

/**
 * Create by munch1182 on 2021/11/8 16:03.
 */
object ThreadPoolHelper {
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
    val cachedPool by lazy {
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
            coreNum,
            NameThread("schedule")
        )

    fun execute(task: Runnable) = cachedPool.execute(task)
    fun remove(task: Runnable) = cachedPool.remove(task)

    fun setExceptionHandler(h: Thread.UncaughtExceptionHandler?) {
        handler = h
    }
}

inline fun pool(submit: Boolean = false, crossinline block: () -> Unit) {
    if (submit) {
        ThreadPoolHelper.cachedPool.submit { block.invoke() }
    } else {
        ThreadPoolHelper.cachedPool.execute { block.invoke() }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun pool(submit: Boolean = false, task: Runnable) {
    if (submit) {
        ThreadPoolHelper.cachedPool.submit(task)
    } else {
        ThreadPoolHelper.cachedPool.execute(task)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> pool(task: Callable<T>): Future<T>? = ThreadPoolHelper.cachedPool.submit(task)

fun thread(
    start: Boolean = true,
    isDaemon: Boolean = false,
    contextClassLoader: ClassLoader? = null,
    name: String? = null,
    priority: Int = -1,
    loop: Boolean = false,
    block: () -> Unit
): Thread {
    val thread = object : Thread() {
        override fun run() {
            if (loop) {
                Looper.prepare()
            }
            block()
            if (loop) {
                Looper.loop()
            }
        }
    }
    if (isDaemon)
        thread.isDaemon = true
    if (priority > 0)
        thread.priority = priority
    if (name != null)
        thread.name = name
    if (contextClassLoader != null)
        thread.contextClassLoader = contextClassLoader
    if (start)
        thread.start()
    return thread
}

@Suppress("NOTHING_TO_INLINE")
inline fun Thread.isMain() = Looper.getMainLooper().thread.id == this.id