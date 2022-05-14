package com.munch.project.one.task

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.extend.bind
import com.munch.lib.extend.get
import com.munch.lib.extend.init
import com.munch.lib.extend.toLive
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.ConfigDialog
import com.munch.lib.fast.view.supportDef
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.task.*
import com.munch.project.one.databinding.LayoutContentOnlyBinding
import com.munch.project.one.databinding.LayoutTaskDialogBinding
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/4/16 20:19.
 */
class TaskActivity : BaseFastActivity(), ActivityDispatch by supportDef({ TaskDialog() }) {

    private val vm by get<TaskVM>()
    private val bind by bind<LayoutContentOnlyBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
        vm.desc().observe(this) { bind.content.text = it }
    }

    class TaskVM : ViewModel() {

        private val keys = mutableListOf<Key>()
        private val addAddKeys = hashMapOf<Key, Key>()
        private var cancel: Key? = null
        private val desc = MutableLiveData("")
        fun desc() = desc.toLive()

        fun add() {
            keys.add(Key(TaskKeyHelper.curr))
            update()
        }

        fun clear() {
            keys.clear()
            addAddKeys.clear()
            update()
        }

        private fun update() {
            desc.postValue(keys.joinToString {
                val add = addAddKeys.getOrDefault(it, null)
                val c = cancel?.takeIf { c -> c == it }
                if (add == null && c == null) {
                    it.toString()
                } else if (add != null && c != null) {
                    "$it($add/$c)"
                } else if (c != null) {
                    "$it(cancel)"
                } else {
                    "$it($add)"
                }
            })
        }

        fun addType(type: Int) {
            when (type) {
                1 -> add1()
                2 -> cancel()
            }
            update()
        }

        private fun cancel() {
            val key = Key(TaskKeyHelper.curr)
            keys.add(key)
            cancel = key
        }

        private fun add1() {
            val key = Key(TaskKeyHelper.curr)
            val addKey = Key(TaskKeyHelper.curr)
            keys.add(key)
            addAddKeys[key] = addKey
        }

        fun start() {
            viewModelScope.launch(Dispatchers.Default) {
                val taskHelper = OrderTaskHelper()
                keys.forEach {
                    val addKey = addAddKeys.getOrDefault(it, null)
                    if (addKey != null) {
                        taskHelper.add(
                            DialogTask(it, addKey,
                                add = { taskHelper.add(DialogTask(addKey)) })
                        )
                    } else if (cancel == it) {
                        taskHelper.add(
                            DialogTask(it, cancel = {
                                viewModelScope.launch(Dispatchers.Default) { taskHelper.cancel() }
                            })
                        )
                    } else {
                        taskHelper.add(DialogTask(it))
                    }
                }
                taskHelper.run()
            }
        }
    }

    class TaskDialog : ConfigDialog() {

        private val bind by add<LayoutTaskDialogBinding>()
        private val vm by get<TaskVM>()

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            bind.taskClear.setOnClickListener { vm.clear() }
            bind.taskAdd.setOnClickListener { vm.add() }
            bind.taskAddType1.setOnClickListener { vm.addType(1) }
            bind.taskAddType2.setOnClickListener { vm.addType(2) }
            bind.taskRun.setOnClickListener { start() }
            vm.desc().observe(this) { desc(it) }
        }

        private fun start() {
            dialog?.dismiss()
            vm.start()
        }

        private fun desc(context: String) {
            bind.taskTask.text = context
        }
    }

    class DialogTask(
        override val key: Key,
        private val addKey: Key? = null,
        private val add: (() -> Unit)? = null,
        private val cancel: (() -> Unit)? = null
    ) : ITask {

        override suspend fun run(input: Data?): Result {
            delay(350L)
            val res = withContext(Dispatchers.Main) {
                suspendCancellableCoroutine<Boolean> { c ->
                    ActivityHelper.curr?.let {
                        AlertDialog.Builder(it)
                            .setTitle("task")
                            .setMessage("this is task $key.")
                            .setPositiveButton(add?.let { "sure($addKey)" } ?: "sure")
                            { dialog, _ ->
                                dialog.cancel()
                                add?.invoke()
                            }
                            .setNegativeButton(cancel?.let { "cancel(*)" } ?: "cancel")
                            { dialog, _ ->
                                dialog.cancel()
                                cancel?.invoke()
                            }
                            .setOnCancelListener { c.resume(true) }
                            .setCancelable(false)
                            .show()
                    } ?: c.resume(false)
                }
            }
            return if (res) Result.success(input) else Result.retry(input)
        }
    }
}