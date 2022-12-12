package com.munch.lib.bluetooth.dev

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import com.munch.lib.bluetooth.connect.*
import com.munch.lib.bluetooth.connect.BluetoothClassicConnectImp
import com.munch.lib.bluetooth.connect.BluetoothLeConnectImp
import com.munch.lib.bluetooth.data.BluetoothDataHelper
import com.munch.lib.bluetooth.data.BluetoothDataReceiver
import com.munch.lib.bluetooth.data.IBluetoothDataHandler
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
abstract class BluetoothDev internal constructor(
    override val mac: String,
    val type: BluetoothType = BluetoothType.UNKNOWN
) : IBluetoothDev, IBluetoothConnector, IBluetoothDataHandler {

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

    val rssiStr: String?
        get() = rssi?.let { "${it}dBm" }
}

internal class BluetoothLeDevice(
    dev: BluetoothDevice,
    private val scan: ScanResult?,
    private val gattHelper: BluetoothGattHelper = BluetoothGattHelper(dev)
) : BluetoothScannedDev(dev), BluetoothLeDev,
    IBluetoothConnector by BluetoothLeConnectImp(dev, gattHelper),
    IBluetoothDataHandler {

    private var dataHelper: BluetoothDataHelper? = null
    private var receiver: BluetoothDataReceiver? = null

    constructor(scan: ScanResult) : this(scan.device, scan)

    // todo 需要在连接开始时才设置BluetoothData相关, 避免浪费, 所以需要回调连接状态更改

    override val rssi: Int?
        get() = scan?.rssi

    override val rawRecord: ByteArray?
        get() = scan?.scanRecord?.bytes

    override val receive: ReceiveChannel<ByteArray>
        get() = TODO("Not yet implemented")

    override suspend fun send(pack: ByteArray) = dataHelper?.send(pack) ?: false

    override fun setDataReceiver(receiver: BluetoothDataReceiver) {
        this.receiver = receiver
    }

}

class BluetoothClassicDevice(dev: BluetoothDevice, rssi: Int?) : BluetoothScannedDev(dev),
    IBluetoothConnector by BluetoothClassicConnectImp(dev) {
    override val receive: ReceiveChannel<ByteArray>
        get() = TODO("Not yet implemented")

    override suspend fun send(pack: ByteArray): Boolean {
        return false
    }

    override fun setDataReceiver(receiver: BluetoothDataReceiver) {
    }
}