package com.munch.lib.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.munch.lib.android.log.LogInfo
import com.munch.lib.android.log.Logger
import com.munch.lib.android.log.log
import org.json.JSONArray
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoggerTest {

    @Test
    fun testJson() {
        val content = arrayOf("123", 1, 1L, 1f, 1.0, arrayOf(true, false))
        // val target = "123, 1, 1L, 1.0F, 1.0, [true, false]"
        log(*content)

        val logger = Logger("test", LogInfo.None)

        val str = JSONArray(MutableList(100) { false }).toString(4)
        logger.log(str)

        assert(true)

    }
}