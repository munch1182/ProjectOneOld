package com.munch.lib.bt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

/**
 * Create by munch1182 on 2021/3/5 14:46.
 */
interface IBtDataOp {

    fun write(byteArray: ByteArray)

    fun read(): ByteArray

}

class BleDataHelper : IBtDataOp {

    fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic) {
        onRead(characteristic.value)
    }

    private fun onRead(value: ByteArray) {
    }

    override fun write(byteArray: ByteArray) {
    }

    override fun read(): ByteArray {
        return byteArrayOf()
    }

    fun setNotify(
        notifyService: BluetoothGattCharacteristic,
        notifyDescriptor: BluetoothGattDescriptor
    ) {
    }

    fun setWrite(writeService: BluetoothGattCharacteristic) {
    }

    fun release() {

    }
}