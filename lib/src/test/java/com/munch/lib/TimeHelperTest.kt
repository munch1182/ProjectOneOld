package com.munch.lib

import com.munch.lib.helper.TimeHelper
import com.munch.lib.helper.getHour
import com.munch.lib.helper.setHMS
import com.munch.lib.helper.toDate
import org.junit.Test
import java.util.*

/**
 * Create by munch1182 on 2021/10/29 09:30.
 */
class TimeHelperTest : BaseTest {

    @Test
    fun test() {
        val current = System.currentTimeMillis()
        log(current.toDate())
        log(TimeHelper.getTime2DescStr(current - 3 * TimeHelper.MILLIS.SECOND))
        log(TimeHelper.getTime2DescStr(current - 3 * TimeHelper.MILLIS.MINUTE))
        log(TimeHelper.getTime2DescStr(current - 3 * TimeHelper.MILLIS.HOUR))
        log(TimeHelper.getTime2DescStr(current - 15 * TimeHelper.MILLIS.HOUR))
        log(TimeHelper.getTime2DescStr(current - 30 * TimeHelper.MILLIS.HOUR))
        log(TimeHelper.getTime2DescStr(current - 50 * TimeHelper.MILLIS.HOUR))
    }

    @Test
    fun test2() {
        val c = Calendar.getInstance()
        val l1 = c.timeInMillis
        val l2 = l1 - (c.getHour()) * TimeHelper.MILLIS.HOUR
        val m1 = TimeHelper.millis2Days(l1)
        val m2 = TimeHelper.millis2Days(l2)
        log(l1, m1, m1.toDate(), l2, m2, m2.toDate())
        log(
            "isOneDay: (${l1.toDate()})-(${l2.toDate()}):" +
                    "${TimeHelper.isOneDay(l1, l2)}"
        )
    }

    @Test
    fun test3() {
        val c = Calendar.getInstance()
        c.setHMS(0,0,0)
        val d = Date(System.currentTimeMillis())
        log(c.timeInMillis.toDate(), d.time.toDate())
        log(c.timeInMillis, d.time, TimeHelper.isOneDay(c, d))
    }
}