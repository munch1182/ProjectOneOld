@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

inline fun Int.hasFlag(flag: Int): Boolean = this and flag == flag

/**
 * 将该数组转为16进制的字符串
 */
fun Number.toHexStr() = Integer.toHexString(this.toInt() and 0xff).completion(2, '0')

/**
 * 将该数据转为至少[len]位的字符串, 如果不足位数, 则在前面补齐[prefix]
 */
fun Number.completion(len: Int, prefix: Char = '0') = toString().completion(len, prefix)

/**
 * 将该值通过16进制的方式转为正值
 */
fun Byte.int() = this.toInt() and 0xff

/**
 * 将该[ByteArray]转为16进制的字符串
 */
fun ByteArray.toHexStr(separator: CharSequence = ", ") =
    this.joinToString(separator) { it.toHexStr() }