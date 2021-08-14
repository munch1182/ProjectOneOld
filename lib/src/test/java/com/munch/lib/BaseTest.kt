package com.munch.lib

/**
 * Create by munch1182 on 2021/8/14 15:55.
 */
interface BaseTest {

    fun log(vararg any: Any?) {
        val sb = StringBuilder()
        sb.append("[")
        var index = 0
        any.forEach {
            if (index > 0) {
                sb.append(", ")
            }
            index++
            sb.append("${it ?: "null"}")
        }
        sb.append("]")
        println(sb.toString())
    }
}