package com.munch.project.launcher.test

import com.munch.pre.lib.dag.Executor
import com.munch.project.launcher.base.DataHelper
import com.munch.project.launcher.base.DataHelper.increment
import com.munch.project.launcher.base.LogTask
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
        private const val ID_TEST = "KEY_TEST"
        private const val KEY_START = "key_start"
    }

    private val time = AtomicLong(0)

    override fun run() {
        val new = DataHelper.test()
        while (true) {
            val now = System.currentTimeMillis()
            if (!new.hasKey(KEY_START)) {
                new.put(KEY_START, 1)
            } else if (time.get() == 0L) {
                new.increment(KEY_START, 2)
            } else {
                val duration = now - time.get()
                if (duration > MIN_15 * 2L) {
                    new.put(now.toString(), true)
                }
            }
            time.set(now)
            Thread.sleep(MIN_15)
        }
    }
}