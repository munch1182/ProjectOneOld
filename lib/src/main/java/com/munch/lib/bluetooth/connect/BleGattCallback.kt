package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/12/7 17:30.
 */
open class BleGattCallback(private val log: Logger) : BluetoothGattCallback() {

    private fun BluetoothGatt?.toStr() =
        if (this == null) "null" else toString().replace("android.bluetooth.BluetoothGatt", "")

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        log.withEnable { "onConnectionStateChange: status: $status, newState: $newState, gatt:${gatt.toStr()}" }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        log.withEnable { "onServicesDiscovered: status:$status, gatt:${gatt.toStr()}" }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        log.withEnable { "onMtuChanged: mtu: $mtu, status: $status, gatt:${gatt.toStr()}" }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        log.withEnable { "onDescriptorRead: status: $status, descriptor:${descriptor?.hashCode()}, gatt:${gatt.toStr()}" }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        log.withEnable { "onDescriptorWrite: status: $status, descriptor:${descriptor?.hashCode()}, gatt:${gatt.toStr()}" }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        log.withEnable { "onCharacteristicWrite: status:$status, characteristic:${characteristic?.hashCode()}, gatt:${gatt.toStr()}" }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        log.withEnable { "onCharacteristicChanged: characteristic:${characteristic?.hashCode()}, gatt:${gatt.toStr()}" }
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        log.withEnable { "onPhyRead: status:$status, txPhy:$txPhy, rxPhy:$rxPhy, gatt:${gatt.toStr()}" }
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        log.withEnable { "onPhyUpdate: status:$status, txPhy:$txPhy, rxPhy:$rxPhy, gatt:${gatt.toStr()}" }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        log.withEnable { "onReadRemoteRssi: status:$status, rssi:$rssi, gatt:${gatt.toStr()}" }
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        super.onReliableWriteCompleted(gatt, status)
        log.withEnable { "onReliableWriteCompleted: status:$status, gatt:${gatt.toStr()}" }
    }

    override fun onServiceChanged(gatt: BluetoothGatt) {
        super.onServiceChanged(gatt)
        log.withEnable { "onServiceChanged: gatt:${gatt.toStr().hashCode()}" }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        log.withEnable { "onCharacteristicRead: status:$status, characteristic:${characteristic?.hashCode()}, gatt:${gatt.toStr()}" }
    }
}