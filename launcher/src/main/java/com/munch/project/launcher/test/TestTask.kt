package com.munch.project.launcher.test

import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.extend.formatDate
import com.munch.project.launcher.base.DataHelper
import com.munch.project.launcher.base.LogTask
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Create by munch1182 on 2021/5/12 9:26.
 */
class TestTask : LogTask() {

    override suspend fun start(executor: Executor) {
        super.start(executor)
        Thread(TestRunnable()).start()
    }

    override val uniqueKey = "test_task_running"
    override val priority = -1
}

class TestRunnable : Runnable {

    companion object {
        private const val MIN_15 = 15L * 60L * 1000L
        private const val KEY_START = "key_start"
        private const val KEY_START_TIME = "key_start_time"
    }

    private val time = AtomicLong(0)
    private val index = AtomicLong(0)

    override fun run() {
        val new = DataHelper.test()
        while (true) {
            val now = System.currentTimeMillis()
            if (!new.hasKey(KEY_START)) {
                new.put(KEY_START, 1)
                new.put(KEY_START_TIME, "yyyy-MM-dd HH:mm:ss".formatDate(Date()))
            } else if (time.get() == 0L) {
                new.increment(KEY_START, 2)
                new.put(
                    "$KEY_START_TIME ${"yyyy-MM-dd HH:mm:ss".formatDate(Date())}",
                    "应用重启导致数据归0"
                )
            } else {
                val duration = now - time.get()
                if (duration > MIN_15 * 2L) {
                    index.incrementAndGet()
                    new.put("yyyy-MM-dd HH:mm:ss".formatDate(now), "应用休眠导致线程未运行")
                } else {
                    new.put(
                        "此时应用仍在运行$index",
                        "yyyy-MM-dd HH:mm:ss".formatDate(now)
                    )
                }
            }
            time.set(now)
            Thread.sleep(MIN_15)
        }
    }
}