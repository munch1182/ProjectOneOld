package com.munch.pre.lib.calender

import com.munch.pre.lib.helper.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/**
 * 日期数据类，主要是操作符重载，以及统一week格式
 *
 * 这些类中，年月日既可以表示具体的年月日，即1990年1月1日，也可以表示计数值，即1年1个月
 *
 * Create by munch1182 on 2021/5/6 16:42.
 */
open class Year(open var year: Int) {

    operator fun plus(year: Year) = this.year + year.year
    operator fun minus(year: Year) = this.year - year.year
    operator fun compareTo(year: Year) = this - year
    operator fun minusAssign(year: Year) {
        this.year = this - year
    }

    operator fun plusAssign(year: Year) {
        this.year = this + year
    }

    override fun toString(): String {
        return "$year y"
    }

    override fun hashCode(): Int {
        return year
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is Year) {
            return other.year == year
        }
        return false
    }
}

open class Month(override var year: Int, open var month: Int) : Year(year) {

    companion object {

        fun now() = from(Calendar.getInstance())

        fun from(calendar: Calendar): Month {
            return Month(calendar.getYear(), calendar.getMonth() + 1)
        }
    }

    open operator fun plus(value: Int): Month {
        var m = this.month + value
        var y: Int
        if (m == 0) {
            y = -1
            m = 12
        } else {
            y = m / 12
            m %= 12
            if (m == 0) {
                m = 12
                y--
            }
        }
        return Month(year + y, m)
    }

    open operator fun plus(month: Month): Month {
        val m = this + month.month
        m.year += month.year
        return m
    }

    open operator fun minus(value: Int): Month {
        return this + (-value)
    }

    open operator fun minus(month: Month): Month {
        val m = this.month - month.month
        return if (m <= 0) {
            val y = m.absoluteValue / 12
            Month(year - y - month.year, m.absoluteValue % 12)
        } else {
            Month(year - month.year, m)
        }
    }

    open operator fun compareTo(month: Month): Int {
        val y = this.year - month.year
        val m = this.month - month.month
        return y * 12 + m
    }

    open operator fun minusAssign(month: Month) {
        update(this - month)
    }

    open operator fun minusAssign(value: Int) {
        update(this - value)
    }

    open operator fun plusAssign(month: Month) {
        update(this + month)
    }

    open operator fun plusAssign(value: Int) {
        update(this + value)
    }

    open fun update(month: Month): Month {
        this.year = month.year
        this.month = month.month
        return this
    }

    fun getYear() = this as Year

    /**
     * 1年3个月=> 15个月
     */
    fun getMonths() = year * 12 + month

    override fun toString(): String {
        return "$year.$month"
    }

    open fun toCalendar(): Calendar = Calendar.getInstance().apply {
        set(year, month - 1, 1, 0, 0, 0)
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is Month) {
            return other.year == year && other.month == month
        }
        return false
    }

    override fun hashCode(): Int {
        return year * 31 + month
    }
}

class MonthHelper(var month: Month, private val instance: Calendar = Calendar.getInstance()) {

    companion object {

        fun Month.getHelper(firstDayOfWeek: Int = Calendar.MONDAY) =
            MonthHelper(this, Calendar.getInstance().apply {
                this.firstDayOfWeek = firstDayOfWeek
            })
    }

    /**
     * 本月第一天的星期数
     */
    private var startWeek: Int = 0
    fun getStartWeek() = startWeek

    /**
     * 本月的天数
     */
    private var days = 0
    fun getDays() = days

    /**
     * 本月最后一天的星期
     */
    private var endWeek = 0
    fun getEndWeek() = endWeek

    /**
     * 本月经历的周数(4、5、6)
     */
    private var weeks = 0
    fun getWeeks() = weeks

    /**
     * 设置的起始星期
     */
    private var firstDayOfWeek = 0
    fun getFirstDayOfWeek() = firstDayOfWeek

    /**
     * 本月的所有天数，共6*7天，本月之外的天数用相邻的补齐并占据对应位置
     * 如果第一天即本月的第一天且本月只有28天，则用下个月的两周补足后面的位置
     */
    private val daysIn42 = ArrayList<Day>(6 * 7)

    init {
        collect()
    }

    fun change(month: Month): MonthHelper {
        if (month == this.month) {
            return this
        }
        this.month = month
        collect()
        return this
    }

    private fun collect() {
        resetIfNeed()
        firstDayOfWeek = instance.firstDayOfWeek
        instance.setDay(1)
        startWeek = instance.getWeek()
        days = instance.getMaxDay()
        instance.setDay(days)
        endWeek = instance.getWeek()
        weeks = collectWeeks()
        collectAllDays()
    }

    private fun collectAllDays() {
        daysIn42.clear()
        var index = 0
        if (startWeek != firstDayOfWeek) {
            val inIndex = getStartWeekInIndex()
            val day = Day(month, 1) - inIndex
            index++
            daysIn42.add(day)
            for (i in 1 until inIndex) {
                index++
                daysIn42.add(day.update(day.day + i))
            }
        }
        for (i in 1..days) {
            index++
            daysIn42.add(Day(this.month, i))
        }
        val nextDay = Day(month + 1, 1)
        index++
        daysIn42.add(nextDay)
        val more = 42 - index + 1
        for (i in 2..more) {
            daysIn42.add(nextDay.update(i))
        }
    }

    /**
     * 需要在[collect]之后使用，返回的是本月第一天到开始星期空缺的天数
     */
    private fun getStartWeekInIndex(): Int {
        if (startWeek == firstDayOfWeek) {
            return 0
        }
        if (startWeek == Calendar.SUNDAY) {
            return 8 - firstDayOfWeek
        }
        return startWeek - firstDayOfWeek
    }

    private fun collectWeeks(): Int {
        //只有28天并且第一天是一周第一天，则此月只有4周
        return if (days == 28 && startWeek == firstDayOfWeek) {
            4
            //有31天且第一天至少是一周最后两天，则此月有6周
        } else if (days == 31 && isLastTwoDays()) {
            6
            //否则只有5周
        } else {
            5
        }
    }

    private fun isLastTwoDays(): Boolean {
        return if (firstDayOfWeek == 1) {
            startWeek == 1 || startWeek == 7
        } else {
            startWeek >= firstDayOfWeek - 1
        }
    }

    private fun resetIfNeed() {
        if (!isSame()) {
            resetNow()
        }
    }

    private fun resetNow() {
        instance.set(month.year, month.month - 1, 1, 0, 0, 0)
    }

    private fun isSame() =
        month.year == instance.getYear() && month.month - 1 == instance.getMonth()


    override fun toString(): String {
        return "$month:{startWeek:$startWeek, endWeek:$endWeek, days:$days, weeks:$weeks, firstDayOfWeek:$firstDayOfWeek, daysIn42=$daysIn42}"
    }

    fun getIndexDay(index: Int): Day = daysIn42[index]
    fun getIndexDays() = daysIn42
}

open class Day(override var year: Int, override var month: Int, open var day: Int) :
    Month(year, month) {

    constructor(month: Month, day: Int) : this(month.year, month.month, day)

    companion object {

        fun now() = from(Calendar.getInstance())

        fun from(calendar: Calendar): Day {
            return Day(calendar.getYear(), calendar.getMonth() + 1, calendar.getDay())
        }
    }

    protected open val instance: Calendar = Calendar.getInstance()

    init {
        resetIfNeed()
    }

    fun getMonth() = this as Month

    fun getWeek(): Int {
        resetIfNeed()
        return instance.getWeek()
    }

    override operator fun plus(value: Int): Day {
        resetIfNeed()
        instance.addDay(value)
        return from(instance)
    }

    operator fun plus(day: Day): Day {
        resetIfNeed()
        return from(instance.apply {
            addDay(day.day)
            addMonth(day.month)
            addYear(day.year)
        })
    }

    override operator fun minus(value: Int): Day {
        return this + (-value)
    }

    operator fun minus(day: Day): Day {
        resetIfNeed()
        return from(instance.apply {
            addDay(-day.day)
            addMonth(-day.month)
            addYear(-day.year)
        })
    }

    /**
     * 相距天数
     */
    operator fun compareTo(day: Day): Int {
        return DateHelper.getDayGapCount(
            instance.timeInMillis,
            day.instance.timeInMillis
        )
    }

    operator fun minusAssign(day: Day) {
        update(this - day)
    }

    override operator fun minusAssign(value: Int) {
        update(this - value)
    }

    operator fun plusAssign(day: Day) {
        update(this + day)
    }

    override operator fun plusAssign(value: Int) {
        update(this + value)
    }

    private fun update(day: Day): Day {
        this.year = day.year
        this.month = day.month
        this.day = day.day
        return this
    }

    /**
     * 更改天数并返回一个新的对象
     *
     * 主要是为了少进行时间的更新
     */
    fun update(day: Int) = Day(year, month, day)

    private fun resetIfNeed() {
        if (!isSame()) {
            resetNow()
        }
    }

    private fun resetNow() {
        instance.set(year, month - 1, day, 0, 0, 0)
        year = instance.getYear()
        month = instance.getMonth() + 1
        day = instance.getDay()
    }

    private fun isSame() =
        year == instance.getYear() && month - 1 == instance.getMonth() && day == instance.getDay()

    override fun toCalendar(): Calendar = Calendar.getInstance().apply {
        set(year, month - 1, day, 0, 0, 0)
    }

    override fun toString(): String {
        return "$year.$month.$day"
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is Day) {
            return other.year == year && other.month == month && other.day == day
        } else if (other is Month) {
            return other == this
        }
        return false
    }

    override fun hashCode(): Int {
        var code = year * 31
        code += month * 31
        return code + day
    }
}