package com.munch.test.lib.pre.thread

import com.munch.lib.fast.base.activity.BaseItemActivity
import com.munch.pre.lib.extend.log
import com.munch.pre.lib.helper.ThreadPoolHelper
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/4/6 11:54.
 */
class ThreadActivity : BaseItemActivity() {

    private val testRun by lazy { TestRunnable() }
    private val oneCode by lazy { ThreadPoolHelper.newFixThread(1) }
    private val fix by lazy { ThreadPoolHelper.newFixThread(2) }
    override fun clickItem(pos: Int) {
        repeat(5) {
            when (pos) {
                0 -> {
                    ThreadPoolHelper.CACHE.execute(testRun)
                }
                1 -> {
                    ThreadPoolHelper.SCHEDULED.schedule(testRun, 1, TimeUnit.SECONDS)
                }
                2 -> {
                    fix.execute(testRun)
                }
                3 -> {
                    oneCode.execute(testRun)
                }
                else -> {
                }
            }
        }
    }

    private class TestRunnable : Runnable {

        private val testValue = AtomicInteger(1)

        override fun run() {
            Thread.sleep(Random.nextLong(1000L))
            log("value:${testValue.getAndIncrement()}")
        }
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf("cache", "schedule", "fix", "one core")
    }
}