package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Create by munch1182 on 2021/8/24 13:41.
 */
sealed class BluetoothType : Parcelable {

    /**
     * 经典蓝牙
     */
    @Parcelize
    object Classic : BluetoothType() {

        override fun toString(): String {
            return "classic"
        }
    }

    /**
     * 低功耗蓝牙
     */
    @Parcelize
    object Ble : BluetoothType() {

        override fun toString(): String {
            return "ble"
        }
    }
}

data class BtDevice(
    val name: String? = null,
    val mac: String,
    val rssi: Int = 0,
    val type: @RawValue BluetoothType,
    val device: BluetoothDevice
) {

    companion object {

        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH])
        fun from(
            device: BluetoothDevice,
            type: @RawValue BluetoothType = BluetoothType.Ble,
            rssi: Int = 0
        ): BtDevice {
            return BtDevice(device.name, device.address, rssi, type, device)
        }

        @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH])
        fun from(mac: String, type: BluetoothType = BluetoothType.Ble): BtDevice? {
            if (!BluetoothAdapter.checkBluetoothAddress(mac)) {
                return null
            }
            val adapter = BluetoothHelper.instance.set.adapter ?: return null
            return from(adapter.getRemoteDevice(mac), type)
        }
    }

    override fun toString(): String {
        return "BtDevice(name=$name, mac='$mac', rssi=$rssi, type=$type)"
    }


}