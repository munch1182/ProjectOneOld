package com.munch.project.test.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import com.munch.lib.dag.Executor
import com.munch.lib.dag.Key
import com.munch.lib.dag.Task
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.TestDialog
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestDialogBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Create by munch1182 on 2021/3/12 10:39.
 */
class TestDialogActivity : TestBaseTopActivity() {

    private val bind by bindingTop<TestActivityTestDialogBinding>(R.layout.test_activity_test_dialog)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.testDialogStart.setOnClickListener { start() }
    }

    private fun start() {
        log("222")
        Executor.getInstance()
            .add(Task1(this))
            .add(Task2(this))
            .add(Task3(this))
            .add(Task4(this))
            .execute()

    }

    class Task1(private val context: Context) : Task {
        override fun start(executor: Executor) {
            log("11111")
            TestDialog.SimpleDialog(context)
                .setContent(uniqueKey.toString())
                .setConfirmListener { }
                .show()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.Main
        override val uniqueKey: Key
            get() = Key("1111")

        override fun dependsOn(): MutableList<Key> {
            return arrayListOf()
        }
    }

    class Task2(private val context: Context) : Task {
        override fun start(executor: Executor) {
            TestDialog.SimpleDialog(context)
                .setContent(uniqueKey.toString())
                .setConfirmListener { }
                .show()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.Main
        override val uniqueKey: Key
            get() = Key("2222")

        override fun dependsOn(): MutableList<Key> {
            return arrayListOf(Key("1111"))
        }
    }

    class Task3(private val context: Context) : Task {
        override fun start(executor: Executor) {
            TestDialog.SimpleDialog(context)
                .setContent(uniqueKey.toString())
                .setConfirmListener { }
                .show()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.Main
        override val uniqueKey: Key
            get() = Key("3333")

        override fun dependsOn(): MutableList<Key> {
            return arrayListOf(Key("2222"))
        }
    }

    class Task4(private val context: Context) : Task {
        override fun start(executor: Executor) {
            TestDialog.SimpleDialog(context)
                .setContent(uniqueKey.toString())
                .setConfirmListener { }
                .show()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.Main
        override val uniqueKey: Key
            get() = Key("4444")

        override fun dependsOn(): MutableList<Key> {
            return arrayListOf(Key("3333"))
        }
    }

}