package com.munch.lib

import com.munch.lib.helper.TimeHelper
import org.junit.Test

/**
 * Create by munch1182 on 2021/10/29 09:30.
 */
class TimeHelperTest : BaseTest {

    @Test
    fun test() {
        val current = System.currentTimeMillis()
        log(TimeHelper.getTime2DescStr(current - 3 * TimeHelper.MILLIS.SECOND))
        log(TimeHelper.getTime2DescStr(current - 3 * TimeHelper.MILLIS.MINUTE))
        log(TimeHelper.getTime2DescStr(current - 3 * TimeHelper.MILLIS.HOUR))
        log(TimeHelper.getTime2DescStr(current - 15 * TimeHelper.MILLIS.HOUR))
        log(TimeHelper.getTime2DescStr(current - 30 * TimeHelper.MILLIS.HOUR))
        log(TimeHelper.getTime2DescStr(current - 50 * TimeHelper.MILLIS.HOUR))
    }
}