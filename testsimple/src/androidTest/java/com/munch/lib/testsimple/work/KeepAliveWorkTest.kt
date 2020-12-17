package com.munch.lib.testsimple.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import com.munch.lib.helper.LogLog
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Create by munch1182 on 2020/12/9 13:46.
 */
@RunWith(AndroidJUnit4::class)
class KeepAliveWorkTest{
    private lateinit var context: Context
    private lateinit var executor: Executor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @Test
    fun testSleepWorker() {
        val worker = TestListenableWorkerBuilder<KeepAliveWork>(context).build()
        runBlocking {
            val result = worker.doWork()
            LogLog.log(result)
        }
    }
}