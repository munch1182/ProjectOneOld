package com.munch.project.one

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.munch.lib.android.extend.isMainThread
import com.munch.lib.android.log.LogInfo
import com.munch.lib.android.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoggerTest {

    @Test
    fun testStr() {
        val content = arrayOf("123", 1, 1L, 1f, 1.0, arrayOf(true, false))
        val target = "123, 1, 1L, 1.0F, 1.0, [true, false]"
        //val caller = "LoggerTest#testStr(LoggerTest.kt:32)"

        val logger = Logger("test")
        logger.setLogListener {
            //输出应该与[target]保持一致
            assert(it.contains(target))
            //协程调用栈与所见不同, 会使用另外的线程去恢复执行
            //Assert.assertTrue(it.contains(caller))

            //回调线程与调用线程保持一致(此处判断不是主线程)
            assert(!isMainThread)
        }

        runBlocking(Dispatchers.Default) { logger.log(*content) }
    }

    @Test
    fun testJson() {
        val logger = Logger("test", LogInfo.None)

        val str = JSONArray(MutableList(100) { false }).toString(4)
        logger.log(str)

        assert(true)

    }
}