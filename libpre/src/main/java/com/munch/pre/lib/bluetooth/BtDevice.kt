package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import kotlinx.android.parcel.Parcelize

/**
 * Create by munch1182 on 2021/3/2 16:55.
 */
@Parcelize
data class BtDevice(
    val name: String? = null,
    val mac: String,
    val rssi: Int = 0,
    val type: BtType,
    val device: BluetoothDevice
) : Parcelable {

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

sealed class BtType : Parcelable {

    /**
     * 经典蓝牙
     */
    @Parcelize
    object Classic : BtType() {

        override fun toString(): String {
            return "classic"
        }
    }

    /**
     * 低功耗蓝牙
     */
    @Parcelize
    object Ble : BtType() {

        override fun toString(): String {
            return "ble"
        }
    }
}