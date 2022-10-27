package com.munch.lib.bluetooth.dev

import android.bluetooth.BluetoothDevice
import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * Create by munch1182 on 2022/10/26 16:28.
 */

internal interface IBluetoothDev {

    /**
     * 一个拥有正确mac地址的对象即一个蓝牙对象
     */
    val mac: String
}

/**
 * 蓝牙设备的类型
 */
sealed class BluetoothType : SealedClassToStringByName() {
    object UNKNOWN : BluetoothType()
    object CLASSIC : BluetoothType()
    object LE : BluetoothType()
    object DUAL : BluetoothType()

    fun from(dev: BluetoothDevice): BluetoothType {
        return when (dev.type) {
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> UNKNOWN
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> CLASSIC
            BluetoothDevice.DEVICE_TYPE_LE -> LE
            BluetoothDevice.DEVICE_TYPE_DUAL -> DUAL
            else -> UNKNOWN
        }
    }
}