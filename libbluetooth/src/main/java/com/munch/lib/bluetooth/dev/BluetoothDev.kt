package com.munch.lib.bluetooth.dev

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import com.munch.lib.android.extend.toHexStr

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
open class BluetoothDev(
    override val mac: String,
    var name: String?,
    private var dev: BluetoothDevice? = null
) : IBluetoothDev {

    fun canConnect() = dev != null

    override fun toString() = mac
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BluetoothDev
        if (mac != other.mac) return false
        return true
    }

    override fun hashCode(): Int {
        return mac.hashCode()
    }
}

/**
 * 扫描到的类, 附带蓝牙广播数据
 */
class BluetoothScanDev(scan: ScanResult) :
    BluetoothDev(scan.device.address, scan.device.name, scan.device) {

    val rssi = scan.rssi

    val rssiStr = "${rssi}dBm"

    val rawRecord = scan.scanRecord?.bytes

    fun getRecords(): List<Record> {
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