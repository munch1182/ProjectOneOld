package com.munch.project.testsimple.alive

import android.content.Context
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

    fun testMix(context: Context) {
        TestHelper.testAliveTime(context, NAME_MIX)
    }

    fun getLastTimeForegroundAliveTime(context: Context): String? {
        val time = SpHelper.getSp(
            context,
            NAME_FOREGROUND
        ).get(TestHelper.KEY_ALIVE_TIME_TEST, 0L)
        if (time == 0L) {
            return null
        }
        return "HH:mm:ss".formatDate(Date(time!! - TIME))
    }

    fun startForegroundTimerThread(context: Context) {
        TestHelper.testAliveTime(
            context,
            NAME_FOREGROUND
        )
    }

    fun startGuardTest(context: Context) {
        SpHelper.getSp(
            context,
            NAME_GUARD
        ).put(KEY_GUARD_START, true)
    }

    fun getGuardCount(context: Context): Int? {
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

    fun guardCount4Keep(context: Context) {
        val sp = SpHelper.getSp(
            context,
            NAME_GUARD
        )
        val count = sp.get(KEY_COUNT_GUARD, 0)!! + 1
        sp.put(KEY_COUNT_GUARD, count)
    }

    fun keepCount4Guard(context: Context) {
        val sp = SpHelper.getSp(
            context,
            NAME_GUARD
        )
        val count = sp.get(KEY_COUNT_KEEP, 0)!! + 1
        sp.put(KEY_COUNT_KEEP, count)
    }

    fun clear(context: Context) {
        SpHelper.getSp(context, NAME_FOREGROUND).clear()
        SpHelper.getSp(context, NAME_GUARD).clear()
        SpHelper.getSp(context, NAME_SILENT).clear()
    }

    fun timerSilentMusic(context: Context, pool: ThreadPoolExecutor) {
        TestHelper.testAliveTime(
            context,
            NAME_SILENT, pool
        )
    }

    fun countWork(context: Context) {
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

    fun getLastWorkTime(context: Context): Long? {
        return SpHelper.getSp(
            context,
            NAME_WORK
        ).get(KEY_WORK_TIME, -1L)!!.takeIf {
            it != -1L
        }

    }

}