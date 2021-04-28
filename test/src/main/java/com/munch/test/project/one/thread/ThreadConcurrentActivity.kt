package com.munch.test.project.one.thread

import android.os.Bundle
import com.munch.pre.lib.helper.ThreadPoolHelper
import com.munch.pre.lib.log.log
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityThreadConcurrentBinding
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/4/28 9:52.
 */
class ThreadConcurrentActivity : BaseTopActivity() {

    private val bind by bind<ActivityThreadConcurrentBinding>(R.layout.activity_thread_concurrent)
    private var func1: ((bytes: ByteArray, index: Int) -> Unit)? = { bytes, index ->
        notify("${bytes.joinToString { s -> String.format("0x%02x", s) }}, $index")
    }

    private fun notify(it: String) {
        runOnUiThread { bind.threadState.text = it }
    }

    private val run = TestRunnable(func1)
    private val pool = ThreadPoolHelper.CACHE_IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this

        bind.threadStart.setOnClickListener { start() }
        bind.threadStop.setOnClickListener { stop() }
        bind.threadTest.setOnClickListener { test() }
        bind.threadTestLoop.setOnClickListener { testLoop() }
        bind.threadTestLoop2.setOnClickListener { testLoop2() }
        bind.threadTestLoop3.setOnClickListener { testLoop3(0) }
    }

    private fun start() {
        run.start(pool)
        notify("start")
    }

    private fun stop() {
        run.stop()
        notify("stop")
    }

    private fun test() {
        run.add(Random.nextBytes(5))
    }

    private fun testLoop() {
        run.add(*Array(5) { Random.nextBytes(5) })
    }

    private fun testLoop2() {
        repeat(5) {
            run.addOne(Random.nextBytes(5))
        }
    }

    private fun testLoop3(count: Int) {
        if (count >= 5) {
            return
        }
        run.func = { bytes, index ->
            notify("${bytes.joinToString { s -> String.format("0x%02x", s) }}, $index")
            testLoop3(count + 1)
        }
        test()
    }

    override fun onDestroy() {
        super.onDestroy()
        func1 = null
        stop()
    }

    private class TestRunnable(var func: ((success: ByteArray, index: Int) -> Unit)?) :
        Runnable {

        private var running = false
        private val lock = Object()
        private val list = LinkedList<ByteArray>()
        private var index = 0

        override fun run() {
            running = true
            while (running) {
                synchronized(lock) {
                    if (list.isEmpty()) {
                        index = 0
                        log("wait")
                        lock.wait()
                    }
                }
                log("notified")
                if (!running) {
                    log("break")
                    break
                }
                val first = list.removeFirst()
                log("first")
                //wait success
                var maxWait = 5
                do {
                    Thread.sleep(1L)
                    maxWait--
                } while (maxWait > 0)
                func?.invoke(first, index++)
            }
        }

        fun add(vararg byteArray: ByteArray) {
            list.addAll(byteArray)
            synchronized(lock) {
                lock.notify()
            }
        }

        fun addOne(byteArray: ByteArray) {
            list.add(byteArray)
            synchronized(lock) {
                lock.notify()
            }
        }

        fun start(pool: ThreadPoolExecutor) {
            pool.execute(this)
        }

        fun stop() {
            running = false
            synchronized(lock) {
                lock.notify()
            }
        }
    }
}