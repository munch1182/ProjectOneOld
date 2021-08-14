package com.munch.lib.helper

/**
 * Create by munch1182 on 2021/8/14 16:06.
 */
private val sb = StringBuilder()

fun Int.toBinaryStr(): String {
    sb.clear()
    var num = this
    while (num > 0) {
        sb.append(num % 2)
        num /= 2
    }
    return sb.reverse().toString()
}