package com.munch.pre.lib.helper.measure

import android.util.SparseArray
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.log.Logger

/**
 * 专用于测量的类
 *
 * 正式版考虑移除相关代码
 *
 * Create by munch1182 on 2021/5/8 17:22.
 */
@DefaultDepend([Logger::class])
open class MeasureTimeHelper {

    protected open val log = Logger().apply {
        tag = "TimeMeasure"
        noInfo = true
        setListener { _, _ ->
            //存进文件中
        }
    }

    protected open val map = SparseArray<Tester>()

    /**
     * 如果要在混淆中移除此类，则需要注意此方法的调用
     */
    open fun measure(tag: String, func: () -> Unit) {
        val start = System.currentTimeMillis()
        func.invoke()
        log.log("$tag: ${System.currentTimeMillis() - start} ms")
    }

    open fun start(any: Any, tag: String, warnTime: Long) = start(tagWithClass(any, tag), warnTime)

    open fun start(tag: String, warnTime: Long) {
        getTimerOrNew(tag, warnTime).start()
    }

    open fun stop(any: Any, tag: String) = stop(tagWithClass(any, tag))

    open fun stop(tag: String) {
        getTimerOrNull(tag)?.stop()
    }

    protected open fun getTimerOrNew(tag: String, warnTime: Long): Timer {
        val key = tag.hashCode()
        return if (map.indexOfKey(key) >= 0) {
            map.get(key)
        } else {
            val timer = Timer(tag, warnTime, log)
            map.put(key, timer)
            timer
        } as Timer
    }

    protected open fun getCounterOrNew(tag: String, warnCount: Int): Counter {
        val key = tag.hashCode()
        return if (map.indexOfKey(key) >= 0) {
            map.get(key)
        } else {
            val timer = Counter(tag, warnCount, log)
            map.put(key, timer)
            timer
        } as Counter
    }

    protected open fun getCounterOrNull(tag: String): Counter? {
        val key = tag.hashCode()
        return if (map.indexOfKey(key) >= 0) {
            map.get(key) as Counter
        } else {
            return null
        }
    }

    protected open fun getTimerOrNull(tag: String): Timer? {
        val key = tag.hashCode()
        return if (map.indexOfKey(key) >= 0) {
            map.get(key) as Timer
        } else {
            return null
        }
    }

    open fun count(any: Any, tag: String, warnCount: Int) = count(tagWithClass(any, tag), warnCount)

    open fun count(tag: String, warnCount: Int) {
        getCounterOrNew(tag, warnCount).count()
    }

    protected open fun tagWithClass(any: Any, tag: String) = "${any.javaClass.simpleName}-$tag"

    fun reset(any: Any, tag: String) {
        getCounterOrNull(tagWithClass(any, tag))?.reset()
    }

    abstract class Tester(protected open val logger: Logger, protected open val tag: String) {
        open fun print(msg: String) {
            logger.log("$tag : $msg")
        }
    }

    /**
     * warnTime：需要输出的最小时间，至少有此时间的才会被输出
     */
    open class Timer(tag: String, protected open val warnTime: Long, logger: Logger) :
        Tester(logger, tag) {

        protected open var start = 0L

        open fun start() {
            start = System.currentTimeMillis()
        }

        open fun stop() {
            val duration = System.currentTimeMillis() - start
            if (duration >= warnTime) {
                print("$duration ms")
            }
        }
    }

    /**
     * warnCount：需要输出的最小次数，至少执行此次数时才会输出
     */
    open class Counter(tag: String, protected open val warnCount: Int, logger: Logger) :
        Tester(logger, tag) {
        protected open var count = 0

        open fun count() {
            count++
            if (warnCount <= count) {
                print("$count times")
            }
        }

        open fun reset() {
            count = 0
        }

    }
}