package com.munch.lib.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission

/**
 * Create by munch1182 on 2021/3/2 16:55.
 */
data class BtDeviceBean(
    val name: String? = null,
    val mac: String,
    val rssi: Int = 0,
    val device: BluetoothDevice
) {

    companion object {

        @RequiresPermission(
            allOf = [android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH]
        )
        fun from(device: BluetoothDevice, rssi: Int): BtDeviceBean {
            return BtDeviceBean(device.name, device.address, rssi, device)
        }

        @RequiresPermission(
            allOf = [android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH]
        )
        fun from(result: ScanResult): BtDeviceBean {
            val device = result.device
            return BtDeviceBean(device.name, device.address, result.rssi, device)
        }
    }

    fun getRssiStr() = "$rssi dBm"

    override fun hashCode(): Int {
        return mac.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is BtDeviceBean) {
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