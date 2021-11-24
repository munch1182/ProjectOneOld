package com.munch.project.one.test

import com.google.gson.Gson
import com.munch.lib.fast.base.BaseBtnFlowActivity
import com.munch.lib.helper.toDate
import com.munch.lib.log.Log2FileHelper
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/10/10 16:15.
 */
class LogActivity : BaseBtnFlowActivity() {

    private val log2FileHelper by lazy { Log2FileHelper(cacheDir) }
    private val l = Logger("LogActivity")

    override fun getData() = mutableListOf("type", "json", "multi", "exception", "write file")

    override fun onClick(pos: Int) {
        super.onClick(pos)
        when (pos) {
            0 -> {
                log("log(1,2,3)")
                log("====>")
                log(1, 2, 3)
                log("log(byteArrayOf(1,2,3)")
                log("====>")
                log(byteArrayOf(1, 2, 3))
                log("log(arrayOf(\"1\", \"2\", \"3\", \"4\", \"5\")")
                log("====>")
                log(arrayOf("1", "2", "3", "4", "5"))
            }
            1 -> {
                val time = System.currentTimeMillis()
                val testJsonBean = TestJsonBean(time.toDate() ?: "", time).toJson()
                log("log($testJsonBean)")
                log("====>")
                log(testJsonBean)
                val testJsonArray = arrayOf("1", "2", "3", "4", "5").toJson()
                log("log($testJsonArray")
                log("====>")
                log(testJsonArray)

            }
            2 -> {
                val sb = StringBuilder()
                repeat(300) { sb.append("1234567890") }
                val str = sb.toString()
                log("log ${str.length} char")
                log("====>")
                log(str)
            }
            3 -> {
                log("log exception")
                log("====>")
                try {
                    throw RuntimeException("exception test log")
                } catch (e: Exception) {
                    log(e)
                }
            }
            4 -> {
                val s = System.currentTimeMillis().toDate() ?: ""
                log2FileHelper.write(s)
                log(
                    "write $s to ${log2FileHelper.currentFile?.absolutePath}"
                )
            }
        }
    }

    private fun log(vararg any: Any) {
        val a = when {
            any.isEmpty() -> ""
            any.size == 1 -> any[0]
            else -> any
        }
        l.methodOffset(1).log(a)
    }
}

data class TestJsonBean(val date: String, val time: Long)

fun Any.toJson(): String = Gson().toJson(this)