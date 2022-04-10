package com.munch.lib.android.extend

import com.munch.lib.extend.*
import junit.framework.TestCase
import java.util.*

/**
 * Create by munch1182 on 2022/4/1 15:28.
 */
class DateKtTest : TestCase() {

    fun testToDaySecs() {
        val c = Calendar.getInstance()
        c.setHMS(23, 55, 21)
        assertEquals(c.toDaySecs(), 86121)
    }

    fun testToHMS() {
        val c = Calendar.getInstance()
        c.setHMS(23, 55, 21)
        val pattern = "HH:mm:ss"
        assertEquals(86121.toHMS().toStr(pattern), c.toStr(pattern))
    }

    fun testToDate() {
        assertEquals(
            "2021-04-01 15:35:00".toCalendar("yyyy-MM-dd HH:mm:ss")?.timeInMillis,
            Calendar.getInstance().set(2021, 4, 1, 15, 35, 0, 0).timeInMillis
        )
    }
}