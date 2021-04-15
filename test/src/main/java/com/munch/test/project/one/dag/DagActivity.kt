package com.munch.test.project.one.dag

import android.os.Bundle
import com.munch.lib.fast.base.dialog.SimpleDialog
import com.munch.pre.lib.dag.Dag
import com.munch.pre.lib.dag.Executor
import com.munch.pre.lib.dag.Task
import com.munch.pre.lib.log.log
import com.munch.pre.lib.extend.obOnResume
import com.munch.pre.lib.helper.AppStatusHelper
import com.munch.pre.lib.log.LogLog
import com.munch.test.project.one.base.BaseItemWithNoticeActivity
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Create by munch1182 on 2021/4/1 15:19.
 */
class DagActivity : BaseItemWithNoticeActivity() {

    private val executor = Executor()

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> log(
                Dag<String>()
                    .addEdge(Dag.Edge(Dag.Point("1"), Dag.Point("4")))
                    .addEdge(Dag.Edge(Dag.Point("1"), Dag.Point("3", 2)))
                    .addEdge(
                        Dag.Edge(
                            Dag.Point("2"),
                            Dag.Point("3", 9, Dag.REPLACE_LOWER_PRIORITY)
                        )
                    )
                    .addEdge(Dag.Edge(Dag.Point("2"), Dag.Point("5", 3)))
                    .addEdge(Dag.Edge(Dag.Point("4"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(
                        Dag.Edge(
                            Dag.Point("5", 10, Dag.REPLACE_HIGHER_PRIORITY),
                            Dag.Point("7")
                        )
                    )
                    .addEdge(Dag.Edge(Dag.Point("6"), Dag.Point("9")))
                    .addEdge(Dag.Edge(Dag.Point("6"), Dag.Point("8", 2)))
                    .addEdge(Dag.Edge(Dag.Point("7"), Dag.Point("8")))
                    .addEdge(Dag.Edge(Dag.Point("7"), Dag.Point("10")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("8")))
                    .generaDag()
            )
            1 -> {
                executor
                    .add(TaskBack1())
                    .add(TaskBack2())
                    .add(TaskBack4())
                    .add(TaskBack5())
                    .add(TaskBack10())
                    .add(TaskBack9())
                    .add(TaskBack8())
                    .add(TaskBack7())
                    .add(TaskBack3())
                    .add(TaskBack6())
                    .setExecuteListener { key, _ ->
                        log("$key executed.")
                    }
                    .setExecutedListener { it.clear() }
                    .execute()
            }
            2 -> {
            }
            3 -> {
            }
            4 -> {
            }
            else -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.release()
        executor.cancel()
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf("test dag", "test dialog")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        obOnResume({ LogLog.setListener { msg, _ -> notice(msg) } }, { LogLog.setListener() })
    }

    abstract class BTaskB : Task() {

        override fun start(executor: Executor) {
            log("${this.uniqueKey} start.")
        }
    }

    class TaskBack1 : BTaskB() {

        companion object {
            const val KEY = "T1"
        }

        override val uniqueKey: String = KEY
        override val coroutineContext: CoroutineDispatcher = Dispatchers.IO
    }

    class TaskBack2 : BTaskB() {

        companion object {
            const val KEY = "T2"
        }

        override val uniqueKey: String = KEY
        override val coroutineContext: CoroutineDispatcher = Dispatchers.Main
    }

    class TaskBack3 : BTaskB() {

        companion object {
            const val KEY = "T3"
        }

        override val uniqueKey: String = KEY
        override fun dependsOn(): MutableList<String> = mutableListOf(TaskBack1.KEY, TaskBack2.KEY)
        override val priority: Int = 1
    }

    class TaskBack4 : BTaskB() {

        companion object {
            const val KEY = "T4"
        }

        override val uniqueKey: String = KEY
        override fun dependsOn(): MutableList<String> = mutableListOf(TaskBack1.KEY)
    }

    class TaskBack5 : BTaskB() {

        companion object {
            const val KEY = "T5"
        }

        override fun start(executor: Executor) {
            super.start(executor)
            runBlocking {
                delay(1000L)
            }
            val res = Random.nextBoolean()
            val name = Thread.currentThread().name
            if (!res) {
                return
            }
            launch(Dispatchers.Main) {
                val topActivity = AppStatusHelper.getTopActivity()
                SimpleDialog.Normal(topActivity!!)
                    .setContent("notice dialog from $KEY in $name")
                    .setSureClickListener({ next() })
                    .setCancelClickListener({ next() })
                    .show()
            }
            //当前task开始等待dialog回应，依赖于本任务的task也会相应等待
            await()
        }

        override val uniqueKey: String = KEY
        override fun dependsOn(): MutableList<String> = mutableListOf(TaskBack2.KEY)
    }

    class TaskBack6 : BTaskB() {

        companion object {
            const val KEY = "T6"
        }

        override val uniqueKey: String = KEY
        override val coroutineContext: CoroutineDispatcher = Dispatchers.IO
        override fun dependsOn(): MutableList<String> =
            mutableListOf(TaskBack3.KEY, TaskBack4.KEY)
    }

    class TaskBack7 : BTaskB() {

        companion object {
            const val KEY = "T7"
        }

        override val uniqueKey: String = KEY
        override val coroutineContext: CoroutineDispatcher = Dispatchers.IO
        override fun dependsOn(): MutableList<String> = mutableListOf(TaskBack5.KEY)
    }

    class TaskBack8 : BTaskB() {

        companion object {
            const val KEY = "T8"
        }

        override val uniqueKey: String = KEY
        override val coroutineContext: CoroutineDispatcher = Dispatchers.IO
        override fun dependsOn(): MutableList<String> =
            mutableListOf(TaskBack6.KEY, TaskBack7.KEY)
    }

    class TaskBack9 : BTaskB() {

        companion object {
            const val KEY = "T9"
        }

        override val uniqueKey: String = KEY
        override val coroutineContext: CoroutineDispatcher = Dispatchers.IO
        override fun dependsOn(): MutableList<String> = mutableListOf(TaskBack6.KEY)
    }

    class TaskBack10 : BTaskB() {

        companion object {
            const val KEY = "T10"
        }

        override val uniqueKey: String = KEY
        override val coroutineContext: CoroutineDispatcher = Dispatchers.IO
        override fun dependsOn(): MutableList<String> = mutableListOf(TaskBack7.KEY)
    }

}