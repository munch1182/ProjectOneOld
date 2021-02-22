package com.munch.lib.lib

import com.munch.lib.helper.DateHelper
import com.munch.lib.helper.formatDate
import org.junit.Test

/**
 * Create by munch1182 on 2021/2/22 11:23.
 */
class DateHelperTest {

    private val timeZone = DateHelper.timeZone0

    @Test
    fun testNewData() {
        val newDate = DateHelper.newDate(hour = 0, min = 0, sec = 0, timeZone = timeZone).time
        println(format(newDate))
        val newDate2 =
            DateHelper.newDate(month = 1, hour = 0, min = 0, sec = 0, timeZone = timeZone).time
        println(format(newDate2))
    }

    @Test
    fun testMill() {
        val newDate =
            DateHelper.newDate(hour = 0, min = 0, sec = 0, mill = 0, timeZone = timeZone).time
        val dayMillis = DateHelper.dayMillis(timeZone = timeZone)
        println(format(newDate))
        println(format(dayMillis))
        assert(newDate == dayMillis)
    }

    @Test
    fun isOneDay() {
        assert(DateHelper.isOneDay(DateHelper.newDate(hour = 23).time, DateHelper.newDate(hour = 0).time))
        assert(!DateHelper.isOneDay(DateHelper.newDate(hour = 23).time, DateHelper.newDate(hour = 24).time))
    }

    private fun format(time: Long): String {
        return DateHelper.PATTERN_DEF.formatDate(time, timeZone)
    }
}