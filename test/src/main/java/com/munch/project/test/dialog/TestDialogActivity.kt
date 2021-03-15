package com.munch.project.test.dialog

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.munch.lib.dag.Executor
import com.munch.lib.dag.Key
import com.munch.lib.dag.Task
import com.munch.lib.helper.AppStatusHelper
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.lib.test.TestDialog
import com.munch.project.test.R
import com.munch.project.test.databinding.TestActivityTestDialogBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.concurrent.thread
import kotlin.random.Random

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
        Executor()
            .add(DialogPermission1())
            .add(DialogPermission2())
            .add(DialogCheck {
                toast("需要升级：$it")
            })
            .add(DialogUpdate())
            .setStartListener { log("start") }
            .setExecuteListener { task, _ -> log("${task.uniqueKey} executed") }
            .setCompleteListener { log("completed") }
            .execute()
    }

    private class DialogPermission1 : DialogTask("p1", "正在请求权限1") {

        var sure = false

        override val confirmListener: (dialog: AlertDialog) -> Unit
            get() = {
                sure = true
                next()
            }
    }

    private class DialogPermission2 :
        DialogTask("p2", "正在请求权限2，此权限只有允许权限1之后才会进行请求", arrayOf("p1")) {

        override fun start(executor: Executor) {
            val task1 = executor.getTask("p1")
            if (task1 is DialogPermission1 && !task1.sure) {
                return
            }
            super.start(executor)
        }
    }

    private class DialogCheck(private val checkNotice: (yes: Boolean) -> Unit) :
        DialogTask("check", "正在后台检查升级") {

        var needUpdate = false

        override val uniqueKey: Key
            get() = Key(key)

        override fun start(executor: Executor) {
            /*super.start(executor)*/
            thread {
                Thread.sleep(1000L)
                needUpdate = Random.nextBoolean()
                checkNotice.invoke(needUpdate)
            }
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.IO

        override fun getPriority(): Int = 1
    }

    private class DialogUpdate :
        DialogTask("update", "需要升级，此弹窗总是最后一个弹出", arrayOf("check", "p1", "p2")) {

        override fun start(executor: Executor) {
            val taskCheck = executor.getTask("check")
            if (taskCheck is DialogCheck && !taskCheck.needUpdate) {
                return
            }
            super.start(executor)
        }
    }

    open class DialogTask(
        protected val key: String,
        private val content: String = "",
        private val depends: Array<String> = arrayOf()
    ) : Task() {

        protected open val confirmListener: (dialog: AlertDialog) -> Unit = {
            next()
        }

        override fun start(executor: Executor) {
            signBlock()
            TestDialog.SimpleDialog(AppStatusHelper.getTopActivity()!!)
                .setContent(content)
                .setConfirmListener(confirmListener)
                .setCancelListener { next() }
                .show()
        }

        override val dispatcher: CoroutineDispatcher
            get() = Dispatchers.Main

        override val uniqueKey: Key
            get() = Key(key)

        override fun dependsOn(): MutableList<Key> {
            return depends.map { Key(it) }.toMutableList()
        }
    }
}