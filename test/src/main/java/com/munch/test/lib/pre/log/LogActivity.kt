package com.munch.test.lib.pre.log

import com.google.gson.Gson
import com.munch.pre.lib.extend.log
import com.munch.pre.lib.extend.logJson
import com.munch.pre.lib.log.LogLog
import com.munch.test.lib.pre.R
import com.munch.test.lib.pre.base.BaseItemWithNoticeActivity


/**
 * Create by munch1182 on 2021/3/31 16:13.
 */
class LogActivity : BaseItemWithNoticeActivity() {

    private val list by lazy {
        arrayOf(
            "str", 1, 1f, 1.0,
            'a', "loglog".encodeToByteArray()
        )
    }

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> log(list)
            1 -> test {
                log("2.test inline :27")
            }
            2 -> logJson(Gson().toJson(TestJsonBean()))
            3 -> logJson(Gson().toJson(arrayListOf(TestJsonBean(), TestJsonBean(), TestJsonBean())))
            4 -> log(RuntimeException("test exception"))
            5 -> log(getString(R.string.test_page))
        }
    }

    inline fun test(func: () -> Unit) {
        log("1.test inline :37")
        func.invoke()
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf("list", "inline", "json", "json2", "error", "multi")
    }

    override fun onResume() {
        super.onResume()
        LogLog.setListener { msg, _ ->
            notice(msg)
        }
    }

    override fun onPause() {
        super.onPause()
        LogLog.setListener()
    }

    private data class TestJsonBean(
        val name: String = "name",
        val age: Int = 0,
        val gander: Int = 0,
        val id: String = "0000000000"
    )
}