package com.munch.pre.lib.calender

import com.munch.pre.lib.helper.*
import kotlinx.parcelize.IgnoredOnParcel
import java.util.*

/**
 * 日期数据类，主要是操作符重载，以及统一week格式
 *
 * Create by munch1182 on 2021/5/6 16:42.
 */
//week 1-7
open class Date(
    open var year: Int,
    open var month: Int,
    open var day: Int,
    open var week: Int,
    open var firstDayOfWeek: Int = 1
) : Comparator<Date> {

    companion object {

        fun now() = from(Calendar.getInstance())

        fun from(instance: Calendar): Date {
            return Date(
                instance.getYear(),
                instance.getMonth(),
                instance.getDay(),
                instance.getWeekInNum(),
                instance.getFirstDayOfWeekInNum()
            )
        }
    }

    @IgnoredOnParcel
    protected val instance: Calendar by lazy {
        Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
        }
    }

    operator fun compareTo(date: Date): Int {
        var compareTo = year - date.year
        if (compareTo == 0) {
            compareTo = month - date.month
            if (compareTo == 0) {
                compareTo = day - date.day
            }
        }
        return compareTo
    }

    /**
     * 当前日期加上对应天数
     */
    operator fun plus(day: Int): Date {
        resetIfNeed()
        instance.add(day, Calendar.DAY_OF_MONTH)
        return update(instance)
    }

    protected open fun resetNow() {
        instance.set(year, month, day)
    }

    protected fun getLastDayOfWeek(): Int {
        return if (firstDayOfWeek == 1) 7 else 6
    }

    protected open fun resetIfNeed() {
        if (!isSame()) {
            resetNow()
        }
    }

    private fun isSame() =
        year == instance.getYear() && month == instance.getMonth() && day == instance.getDay()

    /**
     * 当前日期减去对应天数
     */
    operator fun minus(day: Int) = plus(-day)

    /**
     * 两个时间相减，返回间隔毫秒数
     */
    operator fun minus(date: Date): Long {
        return toCalendar().timeInMillis - date.toCalendar().timeInMillis
    }

    override fun compare(o1: Date?, o2: Date?): Int {
        if (o1 == o2) return 0
        o1 ?: return -1
        o2 ?: return 1
        return (o1 - o2).toInt()
    }

    open fun toCalendar(): Calendar {
        resetIfNeed()
        return instance
    }

    open fun update(instance: Calendar): Date {
        year = instance.get(Calendar.YEAR)
        month = instance.get(Calendar.MONTH)
        day = instance.get(Calendar.DAY_OF_MONTH)
        week = instance.get(Calendar.DAY_OF_WEEK)
        resetIfNeed()
        return this
    }
}

class Month(override var year: Int, override var month: Int, override var firstDayOfWeek: Int) :
    Date(year, month, 1, 0) {

    companion object {

        fun now() = from(Calendar.getInstance())

        fun from(instance: Calendar): Month {
            return Month(
                instance.getYear(),
                instance.getMonth(),
                instance.getFirstDayOfWeekInNum()
            )
        }
    }

    private var startWeek: Int = 0
    private var days = 0
    private var endWeek = 0
    private var weeks = 0

    init {
        collect()
    }

    private fun collect() {
        resetIfNeed()
        startWeek = instance.getWeekInNum()
        days = instance.getMaxDay()
        instance.setDay(days)
        endWeek = instance.getWeekInNum()
        weeks = getWeeks()
    }

    private fun getWeeks(): Int {
        //只有28天并且第一天是一周第一天，则此月只有4周
        return if (days == 28 && startWeek == firstDayOfWeek) {
            4
            //有31天且第一天至少是一周最后两天，则此月有6周
        } else if (days == 31 && startWeek >= getLastDayOfWeek() - 1) {
            6
            //否则只有5周
        } else {
            5
        }
    }

    override fun resetNow() {
        super.resetNow()
        collect()
    }

    override fun update(instance: Calendar): Date {
        val update = super.update(instance)
        collect()
        return update
    }

    //unComplete
    /*operator fun minus(month: Month): Long {
         return (this - (month as Date))
     }*/
}

class Day(
    override var year: Int,
    override var month: Int,
    override var day: Int,
    override var week: Int = 0
) : Date(year, month, day, week) {

    companion object {
        fun now() = from(Calendar.getInstance())

        fun from(instance: Calendar): Day {
            return Day(
                instance.getYear(),
                instance.getMonth(),
                instance.getDay(),
                instance.getWeekInNum()
            )
        }
    }

    init {
        if (week == 0) {
            resetIfNeed()
            week = instance.getWeek()
        }
    }
}