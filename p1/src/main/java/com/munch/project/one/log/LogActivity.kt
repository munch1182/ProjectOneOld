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
import com.munch.lib.fast.view.supportDef
import com.munch.lib.log.LogStyle
import com.munch.lib.log.Logger
import com.munch.lib.log.setOnLog
import com.munch.lib.log.setOnPrint
import com.munch.project.one.databinding.LayoutContentOnlyBinding
import com.munch.project.one.databinding.LayoutLogDialogBinding
import org.json.JSONObject

/**
 * Create by munch1182 on 2022/4/18 16:01.
 */
class LogActivity : BaseFastActivity(), ActivityDispatch by supportDef({ LogDialog() }) {

    private val bind by bind<LayoutContentOnlyBinding>()
    private val vm by get<LogVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
        vm.out().observe(this) {
            bind.content.text = it
        }
    }

    internal class LogDialog : ConfigDialog() {

        private val bind by add<LayoutLogDialogBinding>()
        private val vm by get<LogVM>()

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val map = bind.container.children
                .filter { it is CompoundButton }
                .map { it as CompoundButton }
            map.filterIndexed { index, _ -> index <= 4 }
                .checkOnly(vm.content) { vm.changeContent(it) }
            map.filterIndexed { index, _ -> index >= 5 }
                .checkOnly(vm.style) { vm.changeStyle(it) }
        }
    }

    class LogVM : ViewModel() {
        private val out = MutableLiveData("")
        fun out() = out.toLive()
        private val sb = StringBuilder()
        private val log = Logger().apply {
            setOnPrint { _, log -> sb.append(log).append("\n") }
            setOnLog { out.postValue(sb.toString()) }
        }

        var content = 0
            private set
        var style = 0
            private set

        init {
            print()
        }

        fun changeContent(it: Int) {
            content = it
            print()
        }

        fun changeStyle(it: Int) {
            style = it
            print()
        }

        private fun print() {
            sb.clear()
            when (style) {
                0 -> log.style(LogStyle.NORMAL)
                1 -> log.style(LogStyle.THREAD)
                2 -> log.style(LogStyle.FULL)
                3 -> log.style(LogStyle.NONE)
            }
            when (content) {
                0 -> logNoraml()
                1 -> logMutil()
                2 -> logException()
                3 -> logList()
                4 -> logJson()
            }
        }

        private fun logJson() {
            thread {
                log.log(
                    JSONObject("{\"code\":200,\"data\":{\"a\":1,\"b\":2}}")
                        .toString(4)
                )
            }
        }

        private fun logList() {
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

        private fun logException() {
            try {
                throw UnImplException()
            } catch (e: Exception) {
                log.log(e)
            }
        }

        private fun logMutil() {
            thread {
                val sb = StringBuilder()
                for (i in 0..1000) {
                    sb.append("$i$i$i")
                }
                log.log(sb.toString())
            }
        }

        private fun logNoraml() {
            log.log("abcdefgABCDEFG")
            log.log("123", 123, 123L, 123F, 123.0, 0x01.toByte())
            log.log(this)
        }
    }
}