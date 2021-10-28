package com.munch.lib.helper

import java.text.SimpleDateFormat
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

    object Pattern {

        const val YMD = "yyyy-MM-dd"
        const val HMS = "HH:mm:ss"
        const val YMDHMS = "$YMD $HMS"

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

fun Long.toDate(pattern: String = TimeHelper.Pattern.YMDHMS): String? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(this)
    } catch (e: Exception) {
        null
    }
}

fun String.toDate(pattern: String = TimeHelper.Pattern.YMDHMS): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun String.toCalender(
    pattern: String = TimeHelper.Pattern.YMDHMS,
    zone: TimeZone = TimeZone.getDefault()
): Calendar? {
    return toDate(pattern)?.let { Calendar.getInstance(zone).apply { time = it } }
}

fun Calendar.getYear() = get(Calendar.YEAR)
fun Calendar.getMonth() = get(Calendar.MONTH)
fun Calendar.getDay() = get(Calendar.DAY_OF_MONTH)
fun Calendar.getDate() = get(Calendar.DAY_OF_YEAR)
fun Calendar.getHour() = get(Calendar.HOUR_OF_DAY)
fun Calendar.getMinute() = get(Calendar.MINUTE)
fun Calendar.getSecond() = get(Calendar.SECOND)
fun Calendar.getWeek() = get(Calendar.WEEK_OF_MONTH)