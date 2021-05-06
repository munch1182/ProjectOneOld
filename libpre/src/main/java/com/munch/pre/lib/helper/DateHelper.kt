@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.pre.lib.helper

import android.text.format.DateUtils
import androidx.annotation.IntRange
import java.util.*

/**
 * Create by munch1182 on 2020/12/16 10:47.
 */
object DateHelper {

    private const val INVALID = -1

    const val TIME_HOUR_MIN_SEC = 24 * 60 * 60 * 1000
    const val TIME_EIGHT_HOUR = 8 * 60 * 60 * 1000
    const val PATTERN_DEF = "yyyy-MM-dd HH:mm:ss"

    val timeZone0: TimeZone = TimeZone.getTimeZone("UTC+0")

    /**
     * 获取当月最大天数
     *
     * 另一种方法是设置calendar为下一个月第一天然后回退一天也可获取最大天数
     */
    fun getMaxDayInMouth(year: Int, @IntRange(from = 1, to = 12) month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (isLeapYear(year)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 0
        }
    }

    /**
     * 是否是闰年
     */
    fun isLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

    /**
     * @param type [DateUtils.FORMAT_SHOW_DATE][DateUtils.FORMAT_SHOW_TIME]
     * @see DateUtils.FORMAT_SHOW_DATE
     *
     * @see DateUtils.FORMAT_SHOW_TIME
     */
    fun getDateStr2Now(time: Long, type: Int, minResolution: Long = 0L): CharSequence {
        return DateUtils.getRelativeTimeSpanString(
            time,
            System.currentTimeMillis(),
            minResolution,
            type
        )
    }

    /**
     * 主要作用是使用当前时间自动补齐缺省值，
     * 如果只需要更改一两个值或者更改全部值，不建议直接调用此方法，不如直接调用Calendar的方法效率高
     *
     * 月份已对齐，无需更改
     *
     * 不需要的部分传[INVALID]或者不传，则会使用当前时间的部分
     *
     * 相应的值如果超出相应的范围，则会自动向前或者向后进行调整，如该月只有29天，却传入了30，则会返回下一月的第一天的日期
     */
    fun newDate(
        year: Int = INVALID,
        @IntRange(from = -1L, to = 12L) month: Int = INVALID,
        @IntRange(from = -1L, to = 31L) day: Int = INVALID,
        @IntRange(from = -1L, to = 23L) hour: Int = INVALID,
        @IntRange(from = -1L, to = 59L) min: Int = INVALID,
        @IntRange(from = -1L, to = 59L) sec: Int = INVALID,
        @IntRange(from = -1L, to = 999L) mill: Int = INVALID,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Date {
        val calendar = Calendar.getInstance(timeZone)
        var monthCompat = month
        if (month != INVALID) {
            monthCompat = month - 1
        }
        calendar.set(
            judgeValid(year, Calendar.YEAR, calendar),
            judgeValid(monthCompat, Calendar.MONTH, calendar),
            judgeValid(day, Calendar.DAY_OF_MONTH, calendar),
            judgeValid(hour, Calendar.HOUR_OF_DAY, calendar),
            judgeValid(min, Calendar.MINUTE, calendar),
            judgeValid(sec, Calendar.SECOND, calendar)
        )
        calendar.set(Calendar.MILLISECOND, mill)
        return calendar.time
    }

    /**
     * 判断两个时间是否在同一天
     */
    fun isOneDay(time1: Long, time2: Long): Boolean {
        return getDayGapCount(time1, time2) == 0
    }

    /**
     * 获取两个日期之间间隔天数
     */
    fun getDayGapCount(start: Long, end: Long) = ((end - start) / TIME_HOUR_MIN_SEC).toInt()

    /**
     * 清除时分秒，获取年月日的毫秒值
     * 注意时区
     */
    fun dayMillis(
        time: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        if (time < TIME_HOUR_MIN_SEC) {
            throw UnsupportedOperationException()
        }
        return (timeZone.getOffset(time).toLong() + time) / TIME_HOUR_MIN_SEC * TIME_HOUR_MIN_SEC
    }

    private fun judgeValid(value: Int, filed: Int, calendar: Calendar): Int {
        if (value != INVALID) {
            return value
        }
        return calendar.get(filed)
    }

}

fun Calendar.getYear() = get(Calendar.YEAR)
fun Calendar.setYear(year: Int) = set(Calendar.YEAR, year)
fun Calendar.getMonth() = get(Calendar.MONTH)
fun Calendar.setMonth(month: Int) = set(Calendar.MONTH, month)
fun Calendar.getDay() = get(Calendar.DAY_OF_MONTH)
fun Calendar.setDay(day: Int) = set(Calendar.DAY_OF_MONTH, day)
fun Calendar.getWeek() = get(Calendar.DAY_OF_WEEK)
fun Calendar.getMaxDay() = getMaximum(Calendar.DAY_OF_WEEK)

/**
 * 星期一-星期天安装1-7返回
 */
fun Calendar.getWeekInNum(): Int {
    val week = get(Calendar.DAY_OF_WEEK)
    return if (week == Calendar.SUNDAY) {
        7
    } else {
        week - 1
    }
}

fun Calendar.getFirstDayOfWeekInNum() = if (firstDayOfWeek == Calendar.SUNDAY) 7 else firstDayOfWeek