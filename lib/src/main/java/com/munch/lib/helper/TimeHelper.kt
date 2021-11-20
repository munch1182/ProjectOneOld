@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.helper

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2021/10/27 16:55.
 */
object TimeHelper {

    object MILLIS {
        const val SECOND = 1000L
        const val MINUTE = 60 * SECOND
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
    }

    /**
     *                   a h:mm => 上午10:27
     *                  HH:mm Z => 10:27 +0800
     * yyyy-MM-dd HH:mm:ss EEEE => 2021-10-29 10:27:00 星期五
     */
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

    /**
     * 时间戳转为当天开始的时间戳，即该天00:00的时间戳
     */
    fun millis2Days(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        return millis / MILLIS.DAY * MILLIS.DAY - timeZone.rawOffset
    }

    /**
     * 返回时间值[millis]与[disTime]的差值的最近描述
     *
     * @param millis 要被描述的时间值，单位ms
     * @param disTime 被比较的时间值，单位ms
     * @param str 根据时间值返回时间描述
     * level：
     * 0. 1秒内
     * 1. 1分钟内
     * 2. 1小时内
     * 3. 大于1小时小于24小时，即今天内
     * 4. 大于24小时小于48小时，即昨天内
     * 5. 大于48小时
     */
    fun getTime2DescStr(
        millis: Long,
        disTime: Long = System.currentTimeMillis(),
        str: (level: Int) -> String,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val dis = disTime - millis
        when {
            dis < MILLIS.SECOND -> return str.invoke(0)
            dis < MILLIS.MINUTE -> return str.invoke(1)
            dis < MILLIS.HOUR -> return str.invoke(2)
        }
        //被比较的时间的00:00的时间值
        val start = millis2Days(disTime, timeZone)
        return when {
            millis > start -> str.invoke(3)
            millis > start - MILLIS.DAY -> str.invoke(4)
            else -> str.invoke(5)
        }
    }

    fun getTime2DescStr(
        millis: Long,
        disTime: Long = System.currentTimeMillis(),
        str: (level: Int) -> String
    ) = getTime2DescStr(millis, disTime, str, TimeZone.getDefault())

    fun getTime2DescStr(
        millis: Long,
        disTime: Long = System.currentTimeMillis()
    ) = getTime2DescStr(millis, disTime) {
        when (it) {
            0 -> "刚刚"
            1 -> "${(disTime - millis) / MILLIS.SECOND}秒前"
            2 -> "${(disTime - millis) / MILLIS.MINUTE}分钟前"
            3 -> "今天${String.format("%tR", millis)}"
            4 -> "昨天${String.format("%tR", millis)}"
            else -> String.format("%tF", millis)
        }
    }
}

fun Long.toDate(pattern: String = TimeHelper.Pattern.YMDHMS): String? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(this)
    } catch (e: Exception) {
        null
    }
}

fun Long.getHourMinNumber(): Pair<Long, Long> {
    return this / TimeHelper.MILLIS.HOUR to (this % TimeHelper.MILLIS.HOUR) / TimeHelper.MILLIS.MINUTE
}

fun String.toDate(
    pattern: String = TimeHelper.Pattern.YMDHMS,
    locale: Locale = Locale.getDefault()
): Date? {
    return try {
        SimpleDateFormat(pattern, locale).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun String.toCalender(
    pattern: String = TimeHelper.Pattern.YMDHMS,
    locale: Locale = Locale.getDefault()
): Calendar? {
    return toDate(pattern, locale)?.let { Calendar.getInstance(locale).apply { time = it } }
}

inline fun Calendar.getYear() = get(Calendar.YEAR)
inline fun Calendar.getMonth() = get(Calendar.MONTH)
inline fun Calendar.getDay() = get(Calendar.DAY_OF_MONTH)
inline fun Calendar.getDate() = get(Calendar.DAY_OF_YEAR)
inline fun Calendar.getHour() = get(Calendar.HOUR_OF_DAY)
inline fun Calendar.getMinute() = get(Calendar.MINUTE)
inline fun Calendar.getSecond() = get(Calendar.SECOND)
inline fun Calendar.getWeek() = get(Calendar.WEEK_OF_MONTH)