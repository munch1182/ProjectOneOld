package com.munch.lib.bluetooth.data

import com.munch.lib.android.extend.toHexStr

/**
 * Create by munch1182 on 2022/12/13 15:49.
 */
object BluetoothDataLogHelper {

    private const val SEP = ", "
    private const val LIMIT = 500
    private const val START = 20
    private const val END = 5

    fun ByteArray.toLog(): String {
        return joinToString(SEP, transform = { it.toHexStr() })
    }

    fun ByteArray.toSimpleLog() = simpleData(this)

    private fun simpleData(arrays: ByteArray): String {
        if (arrays.size < LIMIT) {
            return arrays.toLog()
        }
        val sb = StringBuilder("[")
        repeat(START) { sb.append(arrays[it].toHexStr()).append(SEP) }
        sb.append("...")
        val len = arrays.size
        repeat(END) { sb.append(arrays[len - END + it - 1].toHexStr()).append(SEP) }
        sb.append("(").append(len).append(")]")
        return sb.toString()
    }
}