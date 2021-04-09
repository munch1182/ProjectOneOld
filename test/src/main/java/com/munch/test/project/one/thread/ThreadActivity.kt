package com.munch.test.project.one.thread

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.pre.lib.extend.StringHelper
import com.munch.pre.lib.extend.log
import com.munch.pre.lib.extend.obOnResume
import com.munch.pre.lib.helper.ThreadPoolHelper
import com.munch.pre.lib.log.LogLog
import com.munch.test.project.one.base.BaseItemWithNoticeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by munch1182 on 2021/4/6 11:54.
 */
class ThreadActivity : BaseItemWithNoticeActivity() {

    private val testRun by lazy { TestRunnable() }
    private val oneCode by lazy { ThreadPoolHelper.newFixThread(1) }
    private val fix by lazy { ThreadPoolHelper.newFixThread(2) }
    private val count = 5
    private val tempSb = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        obOnResume(
            {
                LogLog.setListener { msg, thread ->
                    tempSb.append("$msg ${thread.name} ${StringHelper.LINE_SEPARATOR}")
                }
            },
            { LogLog.setListener() })
    }

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> {
                repeat(count) {
                    ThreadPoolHelper.CACHE_IO.execute(testRun)
                }
            }
            1 -> {
                repeat(count) {
                    ThreadPoolHelper.SCHEDULED.schedule(testRun, 10, TimeUnit.MILLISECONDS)
                }
            }
            2 -> {
                repeat(count) {
                    fix.execute(testRun)
                }
            }
            3 -> {
                repeat(count) {
                    oneCode.execute(testRun)
                }
            }
            else -> {
            }
        }
        lifecycleScope.launch {
            delay(500L)
            notice(tempSb.toString())
            tempSb.clear()
        }
    }

    private class TestRunnable : Runnable {

        private val testValue = AtomicInteger(1)

        override fun run() {
            log("value:${testValue.getAndIncrement()}")
        }
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf(
            "pool:cache",
            "pool:schedule",
            "pool:fix",
            "pool:one core"
        )
    }
}