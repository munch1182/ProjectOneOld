package com.munch.pre.lib.calender

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.munch.pre.lib.calender.MonthHelper.Companion.getHelper
import com.munch.pre.lib.extend.formatDate
import com.munch.pre.lib.log.log
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


/**
 * Create by munch1182 on 2021/5/7 9:53.
 */
@RunWith(AndroidJUnit4::class)
class DateTest {
    @Test
    fun test() {
        val now = Day.now()
        log(now)
        log("yyyyMMdd HHmmss".formatDate(now.toCalendar().time))
        log(now - 4)
        log(now - Day(0, 0, 6))
        log(now - Day.from(Calendar.getInstance().apply {
            set(0, 0, 6, 0, 0, 0)
        }))
        now += 3
        log(now)
        val m = Month.now()
        log(m.getHelper())
        log(m)
        m -= 5
        log(m)
        m -= 24
        log(m)
        m += 24
        log(m)
        m += 5
        log(m)
        m += 3
        log(m)
        m += 3
        log(m)
        m += 3
        log(m)
        m += 3
        log(m)
        m += 7
        log(m)
        m += 1
        log(m)
        m -= 1
        log(m)
        m += 25
        log(m)
        log(m.getHelper())
    }

    @Test
    fun testMonth() {
        val any = Month(2021, 5).getHelper()
        log(any)
        log(Month(2021, 4).getHelper())
        log(any.change(Month(2021, 2)))
    }

    @Test
    fun testDay() {
        val day = Day.now()
        log(day, day - 3, day + 1)
        val any = Day(2021, 5, 1)
        log(any, any - 1)
    }

    @Test
    fun testMonth2() {
        log(Month.now() == Month(2021, 5))
        log(Month.now() == Day(2021, 5, 4))
        log(Day.now() == Day(2021, 5, 12))
    }
}