package com.munch.lib.helper

import java.util.*
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2021/10/27 16:55.
 */
object TimeHelper {


    object MILLIS {
        const val MINUTE = 60 * 1000L
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
    }

    /**
     * 单位：ms
     */
    fun isOneDay(t1: Long, t2: Long, timeZone: TimeZone = TimeZone.getDefault()): Boolean {
        val interval = (t1 - t2).absoluteValue
        return interval < MILLIS.DAY && millis2Days(t1, timeZone) == millis2Days(t2, timeZone)
    }

    fun millis2Days(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        return (timeZone.getOffset(millis) + millis) / MILLIS.DAY
    }
}