package com.munch.lib.helper

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


    /**
     * @param type [DateUtils.FORMAT_SHOW_DATE][DateUtils.FORMAT_SHOW_TIME]
     * @see DateUtils.FORMAT_SHOW_DATE
     *
     * @see DateUtils.FORMAT_SHOW_TIME
     */
    fun getDateStr2Now(time: Long, type: Int): CharSequence {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), 0, type)
    }

    /**
     * 主要使用当前时间自动补齐缺省值
     * 月份已对齐，无需更改
     *
     * 不需要的部分传[INVALID]或者不传，则会使用当前时间的部分
     *
     * 月如果传0会是会回退一个月
     * 天如果传0也会回退一天
     * 如果天数超出当前月份，则会到下月并相应前进多余的天数
     */
    fun newData(
        year: Int = INVALID,
        @IntRange(from = -1L, to = 12L) month: Int = INVALID,
        @IntRange(from = -1L, to = 31L) day: Int = INVALID,
        @IntRange(from = -1L, to = 23L) hour: Int = INVALID,
        @IntRange(from = -1L, to = 59L) min: Int = INVALID,
        @IntRange(from = -1L, to = 59L) sec: Int = INVALID,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Date {
        val calendar = Calendar.getInstance(timeZone)
        calendar.set(
            judgeValid(year, Calendar.YEAR, calendar),
            judgeValid(month, Calendar.MONTH, calendar) - 1,
            judgeValid(day, Calendar.DAY_OF_MONTH, calendar),
            judgeValid(hour, Calendar.HOUR, calendar),
            judgeValid(min, Calendar.MINUTE, calendar),
            judgeValid(sec, Calendar.SECOND, calendar),
        )
        return calendar.time
    }

    /**
     * 判断两个时间是否在同一天
     */
    fun isOneDay(time1: Long, time2: Long, timeZone: TimeZone = TimeZone.getDefault()): Boolean {
        val interval = time1 - time2
        return interval in -TIME_HOUR_MIN_SEC + 1 until TIME_HOUR_MIN_SEC
                && (dayMillis(time1, timeZone) == dayMillis(time2, timeZone))
    }

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