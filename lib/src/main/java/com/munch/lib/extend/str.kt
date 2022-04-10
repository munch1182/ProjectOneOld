package com.munch.lib.extend

import java.util.regex.Pattern
import kotlin.math.min

/**
 * Create by munch1182 on 2022/3/30 09:51.
 */

/**
 * 统计[str]在字符串中出现的次数，如果未出现，则返回0
 *
 * @param str 需要统计的字符串
 */
fun String.count(str: String): Int {
    val matcher = Pattern.compile(str).matcher(this)
    var count = 0
    while (matcher.find()) {
        count++
    }
    return count
}

/**
 * 将开始位置将剩下的字符串按照数量分割成集合
 *
 * @param count 分割的数量
 * @param start 字符开始位置
 */
fun String.split(count: Int, start: Int = 0): Array<String> {
    val len = length - start
    if (len <= 0) {
        return arrayOf()
    } else if (len < count) {
        return arrayOf(this)
    }
    var s = start
    var e: Int
    val size = len / count + if (len % count == 0) 0 else 1
    return Array(size) {
        e = s + min(count, length - s)
        this.substring(s, e).apply { s = e }
    }

}