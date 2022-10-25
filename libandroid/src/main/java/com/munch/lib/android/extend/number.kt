@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

inline fun Int.hasFlag(flag: Int): Boolean = this and flag == flag

fun Number.toHexStr() = Integer.toHexString(this.toInt() and 0xff).completion(2)

fun ByteArray.toHexStr() = this.joinToString { it.toHexStr() }

fun Number.completion(len: Int) = toString().completion(len)

fun String.completion(len: Int): String {
    val str = toString()
    if (str.length < len) {
        val sb = StringBuilder()
        repeat(len - str.length) { sb.append("0") }
        sb.append(str)
        return sb.toString()
    }
    return str
}