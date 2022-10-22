package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
open class BluetoothDev(
    override val mac: String,
    var name: String?,
    private var dev: BluetoothDevice? = null
) : IBluetoothDev {

    override fun canConnect() = dev != null

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

class BluetoothScanDev(scan: ScanResult) :
    BluetoothDev(scan.device.address, scan.device.name, scan.device) {

    val rssi = scan.rssi

    val rssiStr = "${rssi}dBm"
}