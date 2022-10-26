package com.munch.lib.bluetooth

import com.munch.lib.android.extend.toHexStr

/**
 * @see [https://www.bluetooth.com/specifications/assigned-numbers/]
 * @see [../doc/assigned_numbers_release.pdf]
 *
 * Create by munch1182 on 2022/10/26 9:21.
 */
data class BluetoothScanRecord(val len: Int, val type: Byte, val value: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BluetoothScanRecord
        if (len != other.len) return false
        if (type != other.type) return false
        if (!value.contentEquals(other.value)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = len
        result = 31 * result + type
        result = 31 * result + value.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "($len: (${type.toHexStr()}, ${value.toHexStr("")})"
    }
}