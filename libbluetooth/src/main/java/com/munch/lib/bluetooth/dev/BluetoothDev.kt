package com.munch.lib.bluetooth.dev

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
open class BluetoothDev internal constructor(
    override val mac: String,
    val type: BluetoothType = BluetoothType.UNKNOWN
) : IBluetoothDev {

    internal constructor(dev: BluetoothDevice) : this(dev.address, BluetoothType.from(dev))

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

abstract class BluetoothScannedDev(val dev: BluetoothDevice) : BluetoothDev(dev) {
    val name: String?
        get() = dev.name
    open val rssi: Int?
        get() = null

    val rssiStr:String?
        get() = rssi?.let { "${it}dBm" }
}

internal class BluetoothLeDevice(
    dev: BluetoothDevice, private val scan: ScanResult?
) : BluetoothScannedDev(dev), BluetoothLeDev {

    constructor(scan: ScanResult) : this(scan.device, scan)

    override val rssi: Int?
        get() = scan?.rssi

    override val rawRecord: ByteArray?
        get() = scan?.scanRecord?.bytes

}

class BluetoothClassicDevice(dev: BluetoothDevice, rssi: Int?) : BluetoothScannedDev(dev)
class BluetoothDualDevice(dev: BluetoothDevice, scan: ScanResult?, rssi: Int?) :
    BluetoothScannedDev(dev)