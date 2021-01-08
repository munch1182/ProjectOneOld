package com.munch.lib.test

import android.content.Context
import android.os.Debug
import android.os.SystemClock
import com.munch.lib.BaseApp
import com.munch.lib.helper.LogLog
import com.munch.lib.helper.SpHelper
import com.munch.lib.helper.formatDate
import java.io.FileOutputStream
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Create by munch1182 on 2020/12/10 15:49.
 */
object TestHelper {

    const val KEY_TIME_TEST_START = "key_time_test_start_"
    const val KEY_ALIVE_TIME_TEST_START = "key_alive_time_start"
    const val KEY_ALIVE_TIME_TEST_END = "key_alive_time_end"
    const val KEY_TIME_TEST_COUNT = "key_time_test_count"

    const val KEY_TIME_TEST_RESTART_START = "key_time_test_restart_start_"
    const val KEY_TIME_TEST_DISTANCE = "key_time_test_distance_"
    const val KEY_TIME_TEST_RESTART_COUNT = "key_time_test_restart_count"

    const val NAME_SP_DEF = "time_test"

    private val oneCorePool by lazy {
        ThreadPoolExecutor(
            1, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            SynchronousQueue()
        )
    }

    /**
     * 此方法会延迟android的运行速度，因此时间只能相对地看
     *
     * 此方法需要手动停止
     * @see stopMethodTracing
     *
     * 需要读写权限
     *
     * 会有trace文件在 [Context.getExternalFilesDir] 生成
     * 需要自行导出(可使用adb pull命令直接拉取到项目路径)
     * 可直接使用Profiler查看
     */
    fun startMethodTracing(name: String = "dmtrace.trace") {
        Debug.startMethodTracing(name)
    }

    fun stopMethodTracing() {
        Debug.stopMethodTracing()
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
        var fos: FileOutputStream? = null
        try {
            //位置在app内files文件夹下
            fos = context.openFileOutput(fileName, Context.MODE_APPEND)
            fos.write(
                "yyyyMMdd HH:mm:ss".formatDate(System.currentTimeMillis()).toByteArray()
            )
            val next = System.getProperty("line.separator") ?: "\r\n"
            fos.write(next.toByteArray())
            fos.write(msg.toByteArray())
            fos.write(next.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            LogLog.log(e.message)
        } finally {
            try {
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                    sp.put(KEY_ALIVE_TIME_TEST_END, System.currentTimeMillis())
                }
            }
        }
    }
}