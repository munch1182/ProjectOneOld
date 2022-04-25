@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.extend

import java.text.SimpleDateFormat
import java.util.*

/**
 * Create by munch1182 on 2022/4/1 15:00.
 */
inline fun Calendar.getYear() = get(Calendar.YEAR)

/**
 * 月份已修正
 */
inline fun Calendar.getMonth() = get(Calendar.MONTH) + 1
inline fun Calendar.getDay() = get(Calendar.DAY_OF_MONTH)
inline fun Calendar.getWeekToday() = get(Calendar.DAY_OF_WEEK)
inline fun Calendar.getDate() = get(Calendar.DAY_OF_YEAR)
inline fun Calendar.getHour() = get(Calendar.HOUR_OF_DAY)
inline fun Calendar.getMinute() = get(Calendar.MINUTE)
inline fun Calendar.getSecond() = get(Calendar.SECOND)
inline fun Calendar.getWeekIndex() = get(Calendar.WEEK_OF_MONTH)
inline fun Calendar.setHMS(h: Int, m: Int, s: Int, ms: Int = 0): Calendar {
    set(Calendar.HOUR_OF_DAY, h)
    set(Calendar.MINUTE, m)
    set(Calendar.SECOND, s)
    set(Calendar.MILLISECOND, ms)
    return this
}

/**
 * @param month 月份已修正
 */
inline fun Calendar.set(
    y: Int,
    month: Int,
    d: Int,
    h: Int,
    minute: Int,
    s: Int,
    ms: Int = 0
): Calendar {
    set(y, month - 1, d, h, minute, s)
    set(Calendar.MILLISECOND, ms)
    return this
}

inline fun Calendar.toStr(pattern: String, locale: Locale = Locale.getDefault()) =
    SimpleDateFormat(pattern, locale).format(time)

inline fun Calendar.setMD(month: Int, day: Int) {
    set(getYear(), month - 1, day)
}

fun String.toDate(pattern: String, locale: Locale = Locale.getDefault()): Date? {
    return try {
        SimpleDateFormat(pattern, locale).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun String.toCalendar(pattern: String, locale: Locale = Locale.getDefault()) =
    toDate(pattern, locale)?.toCalendar()

fun Date.toCalendar() = Calendar.getInstance().apply { time = this@toCalendar }

fun Long.toCalendar() = Calendar.getInstance().apply { timeInMillis = this@toCalendar }

/**
 * 将时分秒转为从00:00:00开始计时的秒数
 * @see Int.toHMS
 */
fun Calendar.toDaySecs() = getHour() * 3600 + getMinute() * 60 + getSecond()

/**
 * 将从00:00:00开始计时的时间还原为时分秒并放入Calendar中
 *
 * @see Calendar.toDaySecs
 */
fun Int.toHMS(calendar: Calendar = Calendar.getInstance()): Calendar {
    var time = this
    val h = time / 3600
    time -= h * 3600
    val m = h / 60
    time -= m * 60
    calendar.setHMS(h, m, time)
    return calendar
}

fun Long.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss") =
    try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

fun Calendar.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss") = timeInMillis.toDate(pattern)

fun String.toDate(pattern: String = "yyyy-MM-dd HH:mm:ss") =
    try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }