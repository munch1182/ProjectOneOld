package com.munch.lib.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission

/**
 * Create by munch1182 on 2021/3/2 16:55.
 */
data class BtDevice(
    val name: String? = null,
    val mac: String,
    val rssi: Int = 0,
    val type: BtType,
    val device: BluetoothDevice
) {

    companion object {

        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.BLUETOOTH])
        fun from(device: BluetoothDevice, rssi: Int): BtDevice {
            return BtDevice(device.name, device.address, rssi, BtType.Classic, device)
        }

        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.BLUETOOTH])
        fun from(device: BluetoothDevice): BtDevice {
            return from(device, 0)
        }

        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.BLUETOOTH])
        fun from(result: ScanResult): BtDevice {
            val device = result.device
            return BtDevice(device.name, device.address, result.rssi, BtType.Ble, device)
        }
    }

    fun getRssiStr() = "$rssi dBm"

    override fun hashCode(): Int {
        return mac.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is BtDevice) {
            return this.mac == other.mac
        }
        return false
    }
}

sealed class BtType {

    /**
     * 经典蓝牙
     */
    object Classic : BtType()

    /**
     * 低功耗蓝牙
     */
    object Ble : BtType()
}