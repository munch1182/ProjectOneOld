package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import com.munch.lib.extend.SealedClassToStringByName
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

sealed class BluetoothType : SealedClassToStringByName() {
    object LE : BluetoothType() {
        override val value: Int = BluetoothDevice.DEVICE_TYPE_LE
    }

    object CLASSIC : BluetoothType() {
        override val value: Int = BluetoothDevice.DEVICE_TYPE_CLASSIC
    }

    object DUAL : BluetoothType() {
        override val value: Int = BluetoothDevice.DEVICE_TYPE_DUAL
    }

    object UNKNOWN : BluetoothType() {
        override val value: Int = BluetoothDevice.DEVICE_TYPE_UNKNOWN
    }

    abstract val value: Int

    fun unkonw() = this == UNKNOWN
}

interface IBluetoothDev {
    val mac: String
}

open class BluetoothDev(
    override val mac: String,
    var type: BluetoothType = BluetoothType.UNKNOWN
) : IBluetoothDev, BluetoothHelperFun {

    private var device: BluetoothDevice? = null
    private val helper: BluetoothHelper
        get() = BluetoothHelper
    private val job = SupervisorJob()

    constructor(
        device: BluetoothDevice,
        type: BluetoothType = BluetoothType.UNKNOWN
    ) : this(device.address, type) {
        this.device = device
        device.type
    }

    val isUnknownDev: Boolean
        get() = type.unkonw() || device == null

    /**
     * 设备是否已经在系统蓝牙中配对
     */
    val isPaired: Boolean
        get() = helper.pairedDevs?.find { mac == it.address }
            ?.let { if (device == null) device = it } != null

    /**
     * 如果设备已扫描到, 则直接返回true
     * 否则进行扫描, 并返回扫描结果
     */
    suspend fun find(): Boolean {
        if (!isUnknownDev) return true
        return helper.find(mac)?.also { device = it.device } != null
    }

    /**
     * 请求设备配对
     *
     * 如果已配对, 则直接返回true
     * 否则, 请求配对并返回结果
     */
    suspend fun pair(): Boolean {
        if (!isUnknownDev) return true
        val dev = device ?: return false
        return helper.createBond(dev)
    }

    override fun hashCode(): Int {
        return mac.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BluetoothDev

        if (mac != other.mac) return false

        return true
    }

    override val coroutineContext: CoroutineContext
        get() = helper + job

    override fun toString(): String {
        return "BluetoothScanDev($type ${mac})"
    }
}

class BluetoothScanDev(
    val device: BluetoothDevice,
    type: BluetoothType = BluetoothType.UNKNOWN,
    val rssi: Int = 0
) : BluetoothDev(device, type) {

    constructor(
        scan: ScanResult,
        type: BluetoothType = BluetoothType.UNKNOWN,
    ) : this(scan.device, type, scan.rssi)

    override fun toString(): String {
        return "Dev($type ${device.address} ${rssi}dBm)"
    }
}
