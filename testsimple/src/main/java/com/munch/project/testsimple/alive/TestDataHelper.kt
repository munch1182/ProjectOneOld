package com.munch.project.testsimple.alive

import android.content.Context
import com.munch.lib.BaseApp
import com.munch.lib.helper.SpHelper
import com.munch.lib.helper.formatDate
import com.munch.lib.test.TestHelper
import java.util.*
import java.util.concurrent.ThreadPoolExecutor

/**
 * Create by munch1182 on 2020/12/14 14:25.
 */
object TestDataHelper {

    private const val KEY_COUNT_GUARD = "key_count_guard"
    private const val KEY_COUNT_KEEP = "key_count_keep"
    private const val KEY_GUARD_START = "key_guard_start"
    private const val KEY_WORK_COUNT = "key_work_count"
    private const val KEY_WORK_TIME = "key_work_time"

    private const val NAME_FOREGROUND = "name_foreground"
    private const val NAME_GUARD = "name_guard"
    private const val NAME_SILENT = "name_silent"
    private const val NAME_WORK = "name_work"
    private const val NAME_MIX = "name_mix"
    private const val TIME = 8 * 60 * 60 * 1000

    fun testMix(context: Context = BaseApp.getInstance()) {
        TestHelper.testAliveTime(context, NAME_MIX)
    }

    fun getMixTestData(context: Context): String {
        val builder = StringBuilder()
        var sp = SpHelper.getSp(context, NAME_MIX)
        if (sp.hasKey(TestHelper.KEY_ALIVE_TIME_TEST_END)) {
            val time = sp.get(
                TestHelper.KEY_ALIVE_TIME_TEST_END,
                0L
            )!! - sp.get(TestHelper.KEY_ALIVE_TIME_TEST_START, 0L)!!
            builder.append(
                "服务运行时间:${
                    "HH:mm:ss".formatDate(
                        Date(time), TimeZone.getTimeZone("GMT")
                    )
                },"
            )
        }
        sp = SpHelper.getSp(context, NAME_WORK)
        builder.append("work工作${sp.get(KEY_WORK_COUNT, 0)}次,")
        sp = SpHelper.getSp(context, NAME_GUARD)
        builder.append("守护进程连接${sp.get(KEY_COUNT_GUARD, 0)}次")
        return builder.toString()
    }

    fun getLastTimeForegroundAliveTime(context: Context = BaseApp.getInstance()): String {
        val sp = SpHelper.getSp(context, NAME_FOREGROUND)
        val time = sp.get(
            TestHelper.KEY_ALIVE_TIME_TEST_END,
            0L
        )!! - sp.get(TestHelper.KEY_ALIVE_TIME_TEST_START, 0L)!!
        return "HH:mm:ss".formatDate(Date(time), TimeZone.getTimeZone("GMT"))
    }

    fun startForegroundTimerThread(context: Context = BaseApp.getInstance()) {
        TestHelper.testAliveTime(
            context,
            NAME_FOREGROUND
        )
    }

    fun startGuardTest(context: Context = BaseApp.getInstance()) {
        SpHelper.getSp(
            context,
            NAME_GUARD
        ).put(KEY_GUARD_START, true)
    }

    fun getGuardCount(context: Context = BaseApp.getInstance()): Int? {
        val sp = SpHelper.getSp(
            context,
            NAME_GUARD
        )
        if (!sp.hasKey(KEY_GUARD_START)) {
            return null
        }
        return (sp.get(KEY_COUNT_KEEP, 0)!!).coerceAtLeast(
            sp.get(
                KEY_COUNT_GUARD, 0
            )!!
        )
    }

    fun guardCount4Keep(context: Context = BaseApp.getInstance()) {
        val sp = SpHelper.getSp(
            context,
            NAME_GUARD
        )
        val count = sp.get(KEY_COUNT_GUARD, 0)!! + 1
        sp.put(KEY_COUNT_GUARD, count)
    }

    fun keepCount4Guard(context: Context = BaseApp.getInstance()) {
        val sp = SpHelper.getSp(
            context,
            NAME_GUARD
        )
        val count = sp.get(KEY_COUNT_KEEP, 0)!! + 1
        sp.put(KEY_COUNT_KEEP, count)
    }

    fun clear(context: Context = BaseApp.getInstance()) {
        SpHelper.getSp(context, NAME_FOREGROUND).clear()
        SpHelper.getSp(context, NAME_GUARD).clear()
        SpHelper.getSp(context, NAME_SILENT).clear()
        SpHelper.getSp(context, NAME_MIX).clear()
    }

    fun timerSilentMusic(context: Context = BaseApp.getInstance(), pool: ThreadPoolExecutor) {
        TestHelper.testAliveTime(
            context,
            NAME_SILENT, pool
        )
    }

    fun countWork(context: Context = BaseApp.getInstance()) {
        val sp = SpHelper.getSp(
            context,
            NAME_WORK
        )
        if (!sp.hasKey(KEY_WORK_COUNT)) {
            sp.put(KEY_WORK_COUNT, 1)
        } else {
            sp.put(
                KEY_WORK_COUNT, sp.get(
                    KEY_COUNT_GUARD, 0
                )!! + 1
            )
        }
        sp.put(KEY_WORK_TIME, System.currentTimeMillis())
    }

    fun getLastWorkTime(context: Context = BaseApp.getInstance()): Long? {
        return SpHelper.getSp(
            context,
            NAME_WORK
        ).get(KEY_WORK_TIME, -1L)!!.takeIf {
            it != -1L
        }

    }
}