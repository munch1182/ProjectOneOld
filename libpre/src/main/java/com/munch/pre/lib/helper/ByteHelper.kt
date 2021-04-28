package com.munch.pre.lib.helper

/**
 * Create by munch1182 on 2021/4/27 9:04.
 */

fun ByteArray.format() = joinToString(prefix = "[", postfix = "]") { String.format("0x%02X", it) }
