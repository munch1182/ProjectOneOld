package com.munch.pre.lib.bluetooth

/**
 * Create by munch1182 on 2021/4/27 9:04.
 */
object BytesHelper {

    fun format(bytes: ByteArray) =
        bytes.joinToString(prefix = "[", postfix = "]") { String.format("0x%02x", it) }
}