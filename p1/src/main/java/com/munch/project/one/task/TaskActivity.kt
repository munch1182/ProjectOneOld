package com.munch.project.one.task

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvFv
import com.munch.lib.fast.view.supportDef
import com.munch.lib.log.log
import com.munch.lib.task.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/4/16 20:19.
 */
class TaskActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by fvFv(arrayOf("normal task", "", "order task"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.click { _, index ->
            when (index) {
                0 -> {}
                1 -> orderTask()
            }
        }
    }

    private fun orderTask() {
        lifecycleScope.launch(Dispatchers.IO) {
            val helper = OrderTaskHelper()
            helper
                .add(TestOrderTask(0))
                .add(
                    TestOrderTask(1) {
                        helper.add(TestOrderTask(4) {
                            helper.cancel()
                        })
                    }
                )
                .add(TestOrderTask(2) {
                    helper.add(TestOrderTask(5))
                })
                .add(TestOrderTask(3))
                .run()
        }
    }

    private class TestOrderTask(id: Int, private val i: (suspend () -> Unit)? = null) : Task() {

        override val key: Key = Key(id)
        override suspend fun run(input: Data?): Result {
            delay(Random.nextLong(2000))
            log("task : ${input?.get<Int>("test", null)}")
            i?.invoke()
            return Result.success(input)
        }
    }

}