package com.munch.project.one.task

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.munch.lib.extend.toDate
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.fvFv
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.task.ITask
import com.munch.lib.task.ITaskOrder
import com.munch.lib.task.Key
import com.munch.lib.task.TaskHelper
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/4/16 20:19.
 */
class TaskActivity : BaseFastActivity(), SupportActionBar {

    private val bind by fvFv(arrayOf("normal task", "", "order task"))
    private val taskHelper = TaskHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click { _, index ->
            when (index) {
                0 -> testNormal()
                1 -> testOrder()
            }
        }
    }

    private fun testOrder() {
        taskHelper.add(DialogTask(0))
            .add(DialogTask(1))
            .add(DialogTask(2))
            .add(object : DialogTask(3) {

                override suspend fun run() {
                    super.run()
                    taskHelper.add(ThreadTask(8))
                        .add(ThreadTask(9))
                        .add(ThreadTask(10))
                }
            })
            .add(ThreadTask(4))
            .add(DialogTask(5))
            .add(DialogTask(6))
            .add(ThreadTask(7))
            .run()
    }

    private fun testNormal() {
        taskHelper.add(ThreadTask(11))
            .add(ThreadTask(12))
            .add(ThreadTask(13))
            .add(ThreadTask(14))
            .add(ThreadTask(15))
            .add(ThreadTask(16))
            .run()
    }

    private class ThreadTask(k: Int) : ITask, ITaskOrder {
        override val key: Key = Key(k)

        override val coroutines: CoroutineContext
            get() = Dispatchers.IO

        override val delayTime: Long
            get() = Random.nextLong(3000)

        override suspend fun run() {
        }

        override val orderKey: Key
            get() = Key(1)

    }

    private open class DialogTask(k: Int) : ITask, ITaskOrder {

        override val key: Key = Key(k)

        override suspend fun run() {
            withContext(Dispatchers.Main) {
                suspendCancellableCoroutine<Any?> {
                    ActivityHelper.curr?.let { act ->
                        AlertDialog.Builder(act)
                            .setMessage("$key\n${System.currentTimeMillis().toDate()}")
                            .setOnCancelListener { _ -> it.resume(null) }
                            .show()
                    }
                }
            }
        }

        override val orderKey: Key = Key(1)
    }

}