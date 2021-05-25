package com.munch.pre.lib.helper

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.munch.pre.lib.helper.data.MMKVHelper
import com.munch.pre.lib.log.log
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Create by munch1182 on 2021/5/25 9:32.
 */
@RunWith(AndroidJUnit4::class)
class DataFunHelperTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testFun() {
        MMKVHelper.init(appContext)
        MMKVHelper("ID_TEST").apply {
            update("KEY_TEST_INT", 0, 1)
            update("KEY_TEST_DOUBLE", 0.0, 1)
            toggle("KEY_TEST_BOOL", false)
            /*put("KEY_TEST_FLOAT", 2.5f)*/
            increment("KEY_TEST_FLOAT", 0f)

            log(
                get("KEY_TEST_INT", -1), get("KEY_TEST_DOUBLE", -1.0),
                get("KEY_TEST_BOOL", true), get("KEY_TEST_FLOAT", -1f),
                get("KEY_TEST_FLOAT", -1f)
            )
        }
    }
}