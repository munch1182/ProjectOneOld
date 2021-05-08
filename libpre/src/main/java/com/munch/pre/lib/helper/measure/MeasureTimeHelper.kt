package com.munch.pre.lib.helper.measure

import android.util.SparseArray
import com.munch.pre.lib.DefaultDepend
import com.munch.pre.lib.log.Logger

/**
 * 专用于时间测量的类
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
    }

    protected open val map = SparseArray<Timer>()

    open fun measure(tag: String, func: () -> Unit) {
        val start = System.currentTimeMillis()
        func.invoke()
        print(tag, System.currentTimeMillis() - start)
    }

    open fun start(any: Any, tag: String, warnTime: Long) = start(tagWithClass(any, tag), warnTime)

    open fun start(tag: String, warnTime: Long) {
        val key = tag.hashCode()
        if (map.indexOfKey(key) >= 0) {
            map.get(key)
        } else {
            val timer = Timer(warnTime)
            map.put(key, timer)
            timer
        }.start()
    }

    open fun stop(any: Any, tag: String) = stop(tagWithClass(any, tag))

    protected open fun tagWithClass(any: Any, tag: String) = "${any.javaClass.simpleName}-$tag"

    open fun stop(tag: String) {
        val key = tag.hashCode()
        val timer = if (map.indexOfKey(key) >= 0) {
            map.get(key)
        } else {
            return
        }
        val duration = timer.stop()
        if (duration < timer.warnTime) {
            return
        }
        print(tag, duration)
    }

    protected open fun print(tag: String, duration: Long) {
        log.log("$tag: $duration ms")
        //存进文件中
    }

    /**
     * 需要输出的最小时间，超过此时间的才会被输出
     */
    open class Timer(internal open val warnTime: Long) {
        protected open var start = 0L

        open fun start() {
            start = System.currentTimeMillis()
        }


        open fun stop(): Long {
            return System.currentTimeMillis() - start
        }
    }
}