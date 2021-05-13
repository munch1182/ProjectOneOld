package com.munch.pre.lib.calender

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.munch.pre.lib.helper.addMonth
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
    fun testCreate() {
        log(Year.now(), Month.now(), Day.now(), Day.from(Calendar.getInstance().apply {
            set(1999, Calendar.JANUARY, 1, 1, 1, 1)
        }))
    }

    @Test
    fun testCompute() {
        log(Year.now() + 1, Month.now() + 1, Day.now() + 1, Day(2021, 2, 28) + 1)
        val day = Day.now()
        day += 1
        log(day)
        day += Day(0, 2, 2)
        log(day)
        log(Month(2021, 6) - Day.now())
        log(Day.now() - Month(2021, 6))
        val calendar = Calendar.getInstance()
        calendar.addMonth(1)
        log(Day.from(calendar) - Day.now())
        log(Day(1991, 1, 1) - Year(1992))
    }

    @Test
    fun testMonth() {
        log(
            Month.now() - 4,
            Month.now() - 13,
            Month.now() + 5,
            Month.now() + 13,
            Month.now() - Month.now().apply { year += 1 },
            Month.now() - Month(2021, 6),
            Month.now() - Month(2021, 3)
        )
    }

    @Test
    fun testDay() {
        log(Day(0, 0, 2))
        log(Day(0, 2, 2))
    }

    @Test
    fun testSort() {
        val list = mutableListOf(
            Day.now(),
            Day.now() + 1,
            (Month.now() + 1),
            Month.now() - 1,
            Month.now(),
            Year.now() + 1,
            Year.now() + 5,
            Day.now()
        )
        log(list)
        list.sortBy { it.beMonth() }
        log(list)
        list.sortBy { it.beDay() }
        log(list)
    }
}