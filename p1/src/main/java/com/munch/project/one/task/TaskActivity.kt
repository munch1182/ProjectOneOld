package com.munch.project.one.task

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.*
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.log.log
import com.munch.lib.task.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/4/16 20:19.
 */
class TaskActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

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
                1 -> {
                    taskHelper
                        .add(OrderTask(null))
                        .add(OrderTask(null))
                        .add(OrderTask(null))
                        .add(OrderTask {
                            taskHelper.add(OrderTask(null)).run()
                        })
                        .add(OrderTask(null))
                        .add(OrderTask(null))
                        .add(OrderTask {
                            taskHelper.add(OrderTask {
                                postUI(3000L) {
                                    log(123)
                                }
                            }).run()
                        })
                        .run()
                }
            }
        }
    }

    class NormalTask(tag: Int) : ITask {

        override val key: Key = Key(tag)

        override val delayTime: Long
            get() = Random.nextLong(3000)

        override val coroutines: CoroutineContext
            get() = Dispatchers.IO

        override suspend fun run() {
            log("task $key complete.")
        }
    }

    class OrderTask(private val add: (suspend () -> Unit)? = null) : Task(), ITaskOrder {
        override suspend fun run() {
            val now = (ActivityHelper.currCreate as? AppCompatActivity) ?: return
            withContext(now.lifecycleScope.coroutineContext) {
                suspendCancellableCoroutine<Any> {
                    AlertDialog.Builder(now)
                        .setMessage("now index: $key")
                        .apply {
                            add?.let {
                                setPositiveButton("add") { _, _ -> now.lifecycleScope.launch { add.invoke() } }
                            }
                        }
                        .setOnDismissListener { _ -> it.resume(true) }
                        .show()
                }
            }

        }

        override val orderKey: Key = Key(1000)

    }
}