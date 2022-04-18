package com.munch.project.one.log

import android.os.Bundle
import android.util.ArrayMap
import android.view.View
import android.widget.CompoundButton
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.UnImplException
import com.munch.lib.extend.*
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.ConfigDialog
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.SupportConfigDialog
import com.munch.lib.log.InfoStyle
import com.munch.lib.log.Logger
import com.munch.lib.task.thread
import com.munch.project.one.databinding.LayoutContentOnlyBinding
import com.munch.project.one.databinding.LayoutLogDialogBinding

/**
 * Create by munch1182 on 2022/4/18 16:01.
 */
class LogActivity : BaseFastActivity(),
    ActivityDispatch by (SupportActionBar + SupportConfigDialog({ LogDialog() })) {

    private val bind by bind<LayoutContentOnlyBinding>()
    private val vm by get<LogVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
        vm.out().observe(this) {
            bind.content.text = it
        }
    }

    class LogDialog : ConfigDialog() {

        private val bind by add<LayoutLogDialogBinding>()
        private val vm by get<LogVM>()

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val map = bind.container.children.filter { it is CompoundButton }
            map.filterIndexed { index, _ -> index <= 3 }
                .map { it as CompoundButton }.toList()
                .checkOnly {
                    vm.changeContent(it)
                }
            map.filterIndexed { index, _ -> index >= 4 }
                .map { it as CompoundButton }.toList()
                .checkOnly {
                    vm.changeStyle(it)
                }
        }
    }

    class LogVM : ViewModel() {
        private val out = MutableLiveData("")
        fun out() = out.toLive()
        private val sb = StringBuilder()
        private val log = Logger().apply {
            setOnPrintListener(object : Logger.OnPrintListener {
                override fun onPrint(tag: String, log: String) {
                    sb.append(log).append("\n")
                }
            })
            setOnLogListener(object : Logger.OnLogListener {
                override fun onLog(
                    log: String,
                    tag: String,
                    thread: Thread?,
                    stack: Array<String>?
                ) {
                    out.postValue(sb.toString())
                }
            })
        }

        private var content = 0

        fun changeContent(it: Int) {
            content = it
            print()
        }

        fun changeStyle(it: Int) {
            when (it) {
                4 -> log.style(InfoStyle.NULL)
                5 -> log.style(InfoStyle.NORMAL)
                6 -> log.style(InfoStyle.THREAD_ONLY)
                7 -> log.style(InfoStyle.FULL)
            }
            print()
        }

        private fun print() {
            sb.clear()
            when (content) {
                0 -> {
                    log.log("abcdefgABCDEFG")
                    log.log("123", 123, 123L, 123F, 123.0)
                    log.log(this)
                }
                1 -> {
                    thread {
                        val sb = StringBuilder()
                        for (i in 0..1000) {
                            sb.append("$i$i$i")
                        }
                        log.log(sb)
                    }
                }
                2 -> {
                    try {
                        throw UnImplException()
                    } catch (e: Exception) {
                        log.log(e)
                    }
                }
                3 -> {
                    thread {
                        log.log(Array(30) { "${it * 2}" })
                        log.log(Array(11) { "${it * 2}" }.toList())
                        log.log(ArrayMap<Int, Int>().apply {
                            repeat(50) {
                                put(it, it * 2)
                            }
                        })
                    }
                }
            }
        }

    }
}