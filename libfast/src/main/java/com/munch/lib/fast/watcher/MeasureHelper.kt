package com.munch.lib.fast.watcher

import android.util.SparseArray
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/9/15 11:05.
 */
object MeasureHelper {

    /**
     * 超过此时间的Activity加载时间将会被输出到logcat
     */
    var activityMeasureHelper = 500L

    val log by lazy {
        Logger().apply {
            tag = "measure"
            noStack = true
            noInfo = true
        }
    }

    private val timers by lazy { SparseArray<Long>() }

    private fun start(key: Int) {
        timers.put(key, System.currentTimeMillis())
    }

    private fun cost(key: Int): Long {
        val start = timers.get(key) ?: return -1L
        val cost = System.currentTimeMillis() - start
        timers.remove(key)
        return cost
    }

    /**
     * 开始计时
     */
    fun start(key: String) = start(key.hashCode())

    /**
     * 返回从开始点到此时的耗时
     */
    fun cost(key: String) = cost(key.hashCode())

    /**
     * 如果耗时超过[min],则会回调[moreTime]
     */
    fun cost(key: String, min: Long, moreTime: (cost: Long) -> Unit) {
        val cost = cost(key)
        if (cost > min) {
            moreTime.invoke(cost)
        }
    }

}