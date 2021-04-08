package com.munch.test.lib.pre

import android.os.SystemClock
import com.munch.pre.lib.extend.log
import com.munch.test.lib.pre.base.BaseTestActivity
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * Create by munch1182 on 2021/4/2 10:33.
 */
class TestActivity : BaseTestActivity() {

    private val cd = CountDownLatch(1)

    override fun testFun0() {
        thread {
            log(11)
            SystemClock.sleep(3300L)
            cd.countDown()
            log(22)
        }
        cd.await()

        GlobalScope.launch {
            log(1)
            yield()

            launch(Dispatchers.Unconfined) {
                log(2)
                delay(1000L)

                log(3)
            }
            launch(Dispatchers.IO) {
                log(4)
                delay(1000L)
                log(5)
            }

            withContext(Dispatchers.Main){
                log(6)
            }
            log(7)
        }
    }

}