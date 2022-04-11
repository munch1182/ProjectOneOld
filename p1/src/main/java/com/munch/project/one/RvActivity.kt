package com.munch.project.one

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.munch.lib.UnSupportException
import com.munch.lib.log.log
import com.munch.lib.task.ITask
import com.munch.lib.task.TaskHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/3/31 22:39.
 */
class RvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskHelper = TaskHelper()
        taskHelper
            .addTask(object : ITask {
                override suspend fun run() {
                    log(1)
                }
            })
            .addTask(object : ITask {
                override val coroutines: CoroutineContext
                    get() = Dispatchers.Default

                override suspend fun run() {
                    delay(3000L)
                    log(2)
                }
            })
            .addTask(object : ITask {
                override suspend fun run() {
                    log(3)
                    throw UnSupportException()
                }
            })
            .addTask(object : ITask {
                override suspend fun run() {
                    log(4)
                }
            })
            .run()
        taskHelper.addTask(object : ITask {
            override val coroutines: CoroutineContext
                get() = Dispatchers.Default

            override suspend fun run() {
                delay(5000L)
                log(5)
            }
        }).run()

    }
}
