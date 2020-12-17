package com.munch.lib.test

import android.content.Context
import android.os.SystemClock
import com.munch.lib.BaseApp
import com.munch.lib.helper.LogLog
import com.munch.lib.helper.SpHelper
import com.munch.lib.helper.formatDate
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2020/12/10 15:49.
 */
object TestHelper {

    const val KEY_TIME_TEST_START = "key_time_test_start_"
    const val KEY_ALIVE_TIME_TEST = "key_alive_time"
    const val KEY_ALIVE_TIME_TEST_START = "key_alive_time_start"
    const val KEY_TIME_TEST_COUNT = "key_time_test_count"

    const val KEY_TIME_TEST_RESTART_START = "key_time_test_restart_start_"
    const val KEY_TIME_TEST_DISTANCE = "key_time_test_distance_"
    const val KEY_TIME_TEST_RESTART_COUNT = "key_time_test_restart_count"

    const val NAME_SP_DEF = "time_test"

    private val oneCorePool by lazy {
        ThreadPoolExecutor(
            1, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue<Runnable>()
        )
    }

    /**
     * 测试重试，主要用于运行-断开-重启的情形
     */
    fun testRestart(context: Context = BaseApp.getInstance(), spName: String = NAME_SP_DEF) {
        val sp = SpHelper.getSp(context, spName)
        val map = HashMap<String, Any>()
        if (!sp.hasKey(KEY_TIME_TEST_RESTART_COUNT)) {
            map[KEY_TIME_TEST_RESTART_COUNT] = 0
            map["${KEY_TIME_TEST_RESTART_START}0"] = System.currentTimeMillis()
        } else {
            val count = sp.get(KEY_TIME_TEST_RESTART_COUNT, 0)!!
            val i = count + 1
            map[KEY_TIME_TEST_RESTART_COUNT] = i
            map["${KEY_TIME_TEST_RESTART_START}i"] = System.currentTimeMillis()
            val lastTime =
                sp.get("${KEY_TIME_TEST_RESTART_START}count", System.currentTimeMillis())!!
            map["${KEY_TIME_TEST_DISTANCE}i"] = System.currentTimeMillis() - lastTime
        }
        sp.put(map)
    }

    /**
     * 将信息存入文件
     */
    fun saveInFile(context: Context = BaseApp.getInstance(), msg: String, fileName: String) {
        try {
            //位置在app内files文件夹下
            val fos = context.openFileOutput(fileName, Context.MODE_APPEND)
            fos.write(
                "yyyyMMdd HH:mm:ss".formatDate(System.currentTimeMillis()).toByteArray()
            )
            val next = System.getProperty("line.separator") ?: "\r\n"
            fos.write(next.toByteArray())
            fos.write(msg.toByteArray())
            fos.write(next.toByteArray())
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            LogLog.log(e.message)
        }
    }

    /**
     * 时间间距测试开始
     * 可保存每次测试的数据
     * @see testTimeEnd
     * @see SpHelper
     */
    fun testTimeStart(context: Context = BaseApp.getInstance(), spName: String = NAME_SP_DEF) {
        val sp = SpHelper.getSp(context, spName)
        val count = sp.get(KEY_TIME_TEST_COUNT, 1)
        sp.put(KEY_TIME_TEST_START + count, System.currentTimeMillis())
        sp.put(KEY_TIME_TEST_COUNT, count)
    }

    /**
     * 时间间距测试结束
     * @see testTimeStart
     * @see SpHelper
     */
    fun testTimeEnd(context: Context = BaseApp.getInstance(), spName: String = NAME_SP_DEF) {
        val sp = SpHelper.getSp(context, spName)
        var count = sp.get(KEY_TIME_TEST_COUNT, -1)
        if (count == -1) {
            return
        }
        val start = sp.get(KEY_TIME_TEST_START + count, -1)
        if (start != -1) {
            sp.put(KEY_TIME_TEST_START + count, System.currentTimeMillis() - start!!)
            count = count!! + 1
            sp.put(KEY_TIME_TEST_COUNT, count)
        }
    }

    /**
     * 测试存活时间，每1m更新一次
     */
    fun testAliveTime(
        context: Context = BaseApp.getInstance(),
        spName: String,
        pool: ThreadPoolExecutor = oneCorePool
    ) {
        pool.execute {
            while (true) {
                SystemClock.sleep(60 * 1000L)
                val sp = SpHelper.getSp(context, spName)
                if (!sp.hasKey(KEY_ALIVE_TIME_TEST_START)) {
                    sp.put(KEY_ALIVE_TIME_TEST_START, System.currentTimeMillis())
                } else {
                    val time = System.currentTimeMillis() - sp.get(KEY_ALIVE_TIME_TEST_START, 0L)!!
                    sp.put(KEY_ALIVE_TIME_TEST, time)
                }
            }
        }
    }
}