package com.munch.project.one.task

import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.SupportConfigDialog
import com.munch.lib.fast.view.fvFv
import com.munch.lib.log.log
import com.munch.lib.task.ITask
import com.munch.lib.task.Key
import com.munch.lib.task.TaskHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/4/16 20:19.
 */
class TaskActivity : BaseFastActivity(),
    ActivityDispatch by (SupportActionBar + SupportConfigDialog()) {

    private val bind by fvFv(arrayOf("normal task", "", "order task"))
    private val taskHelper = TaskHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click { _, index ->
            when (index) {
                0 -> {
                    repeat(11) { taskHelper.add(NormalTask(it)) }
                    taskHelper.run()
                }
                1 -> {}
            }
        }
    }

    class NormalTask(tag: Int) : ITask {

        override val key: Key = Key(tag)

        override val coroutines: CoroutineContext
            get() = Dispatchers.IO

        override suspend fun run() {
            val timeMillis = Random.nextLong(3000)
            delay(timeMillis)
            log("delay $timeMillis, task $key complete.")
        }
    }
}