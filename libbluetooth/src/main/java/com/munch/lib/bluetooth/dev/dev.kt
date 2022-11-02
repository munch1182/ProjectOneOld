package com.munch.lib.bluetooth.dev

import android.bluetooth.BluetoothDevice
import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.android.extend.toHexStr

/**
 * Create by munch1182 on 2022/10/26 16:28.
 */

interface IBluetoothDev {

    /**
     * 一个拥有正确mac地址的对象即一个蓝牙对象
     */
    val mac: String
}

/**
 * 蓝牙设备的类型
 */
sealed class BluetoothType : SealedClassToStringByName() {
    object UNKNOWN : BluetoothType()
    object CLASSIC : BluetoothType()
    object LE : BluetoothType()
    object DUAL : BluetoothType()

    companion object {
        fun from(dev: BluetoothDevice): BluetoothType {
            return when (dev.type) {
                BluetoothDevice.DEVICE_TYPE_UNKNOWN -> UNKNOWN
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> CLASSIC
                BluetoothDevice.DEVICE_TYPE_LE -> LE
                BluetoothDevice.DEVICE_TYPE_DUAL -> DUAL
                else -> UNKNOWN
            }
        }
    }
}

interface BluetoothLeDev : IBluetoothDev {

    val rawRecord: ByteArray?

    val records: List<Record>
        get() {
            val bytes = rawRecord ?: return emptyList()
            var index = 0
            val list = mutableListOf<Record>()
            while (index <= bytes.size) {
                val len = (bytes[index].toInt() and 0xff)
                if (len == 0) break
                index++
                val type = bytes[index]
                index++
                val array = ByteArray(len - 1)
                repeat(len - 1) { array[it] = bytes[index++] }
                list.add(Record(len, type, array))
            }
            return list
        }


    /**
     * @see [https://www.bluetooth.com/specifications/assigned-numbers/]
     * @see [../doc/assigned_numbers_release.pdf]
     *
     * Create by munch1182 on 2022/10/26 9:21.
     */
    data class Record(val len: Int, val type: Byte, val value: ByteArray) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Record
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
}