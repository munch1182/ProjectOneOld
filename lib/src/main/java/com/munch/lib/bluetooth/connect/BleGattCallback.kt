package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/12/7 17:30.
 */
abstract class BleGattCallback(private val mac: String?, private val log: Logger) :
    BluetoothGattCallback() {

    private fun BluetoothGatt?.toStr() =
        if (this == null) "null" else toString().replace("android.bluetooth.BluetoothGatt", "")

    private fun BluetoothGattDescriptor?.toStr() =
        if (this == null) "null" else toString().replace(
            "android.bluetooth.BluetoothGattDescriptor", ""
        )

    private fun BluetoothGattCharacteristic?.toStr() =
        if (this == null) "null" else toString().replace(
            "android.bluetooth.BluetoothGattCharacteristic", ""
        )

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        log.withEnable { "$mac: onConnectionStateChange: status: ${status(status)}, newState: $newState, gatt: ${gatt.toStr()}." }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        log.withEnable { "$mac: onServicesDiscovered: status: ${status(status)}, gatt: ${gatt.toStr()}." }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        log.withEnable { "$mac: onMtuChanged: mtu: $mtu, status: ${status(status)}, gatt: ${gatt.toStr()}." }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        log.withEnable { "$mac: onDescriptorRead: status: ${status(status)}, descriptor: ${descriptor.toStr()}, gatt: ${gatt.toStr()}." }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        log.withEnable { "$mac: onDescriptorWrite: status: ${status(status)}, descriptor: ${descriptor.toStr()}, gatt: ${gatt.toStr()}." }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        log.withEnable { "$mac: onCharacteristicWrite: status: ${status(status)}, characteristic: ${characteristic.toStr()}, gatt: ${gatt.toStr()}." }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        log.withEnable { "$mac: onCharacteristicChanged: characteristic: ${characteristic.toStr()}, gatt: ${gatt.toStr()}." }
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        log.withEnable { "$mac: onPhyRead: status: ${status(status)}, txPhy: $txPhy, rxPhy: $rxPhy, gatt: ${gatt.toStr()}." }
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        log.withEnable { "$mac: onPhyUpdate: status: ${status(status)}, txPhy: $txPhy, rxPhy: $rxPhy, gatt: ${gatt.toStr()}." }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        log.withEnable { "$mac: onReadRemoteRssi: status: ${status(status)}, rssi: $rssi, gatt: ${gatt.toStr()}." }
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        super.onReliableWriteCompleted(gatt, status)
        log.withEnable { "$mac: onReliableWriteCompleted: status: ${status(status)}, gatt: ${gatt.toStr()}." }
    }

    override fun onServiceChanged(gatt: BluetoothGatt) {
        super.onServiceChanged(gatt)
        log.withEnable { "$mac: onServiceChanged: gatt: ${gatt.toStr().hashCode()}." }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        log.withEnable { "$mac: onCharacteristicRead: status: ${status(status)}, characteristic: ${characteristic.toStr()}, gatt: ${gatt.toStr()}." }
    }

    private fun status(status: Int): String {
        return when (status) {
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> "GATT_READ_NOT_PERMITTED"
            BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "GATT_WRITE_NOT_PERMITTED"
            BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "GATT_INSUFFICIENT_AUTHENTICATION"
            BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "GATT_REQUEST_NOT_SUPPORTED"
            BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "GATT_INSUFFICIENT_ENCRYPTION"
            BluetoothGatt.GATT_INVALID_OFFSET -> "GATT_INVALID_OFFSET"
            BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT_INVALID_ATTRIBUTE_LENGTH"
            BluetoothGatt.GATT_CONNECTION_CONGESTED -> "GATT_CONNECTION_CONGESTED"
            else -> status.toString()
        }
    }
}