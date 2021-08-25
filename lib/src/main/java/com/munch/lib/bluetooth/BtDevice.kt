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

    val rssiStr: String
        get() = "$rssi dBm"

    fun connect() {
        BluetoothHelper.instance.connect(this)
    }

    fun disconnect() {
        BluetoothHelper.instance.disconnect()
    }

    /**
     * 使用反射判断该蓝牙设备是否已被系统连接，如果蓝牙已关闭、未获取到则返回null，否则返回boolean
     */
    fun isConnected(): Boolean? {
        return try {
            val isConnected = BluetoothDevice::class.java.getDeclaredMethod("isConnected")
            isConnected.isAccessible = true
            isConnected.invoke(device) as? Boolean?
        } catch (e: Exception) {
            null
        }
    }

    override fun toString(): String {
        return "BtDevice(name=$name, mac='$mac', rssi=$rssi, type=$type)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BtDevice

        if (mac != other.mac) return false

        return true
    }

    override fun hashCode(): Int {
        return mac.hashCode()
    }
}