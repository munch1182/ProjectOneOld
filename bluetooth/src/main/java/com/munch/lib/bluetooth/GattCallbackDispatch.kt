package com.munch.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.munch.lib.Destroyable
import com.munch.lib.helper.ARSHelper
import com.munch.lib.log.Logger

class GattCallbackDispatch(private val log: Logger) : BluetoothGattCallback(), Destroyable {

    val onStateChange = ARSHelper<OnStateChange>()
    val onServiceDiscover = ARSHelper<OnServicesDiscovered>()
    val onMtuChange = ARSHelper<OnMtuChanged>()
    val onDescriptorRead = ARSHelper<OnDescriptorRead>()
    val onDescriptorWrite = ARSHelper<OnDescriptorWrite>()
    val onCharacteristicRead = ARSHelper<OnCharacteristicRead>()
    val onCharacteristicWrite = ARSHelper<OnCharacteristicWrite>()
    val onCharacteristicChanged = ARSHelper<OnCharacteristicChanged>()
    val onReadRemoteRssi = ARSHelper<OnReadRemoteRssi>()

    override fun destroy() {
        onStateChange.clear()
        onServiceDiscover.clear()
        onServiceDiscover.clear()
    }

    private fun fmtStatus(status: Int): String {
        return "${
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> "SUCCESS"
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> "READ_NOT_PERMITTED"
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> "CONNECTION_CONGESTED"
                BluetoothGatt.GATT_FAILURE -> "FAILURE"
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "INSUFFICIENT_AUTHENTICATION"
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "INSUFFICIENT_ENCRYPTION"
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "INVALID_ATTRIBUTE_LENGTH"
                BluetoothGatt.GATT_INVALID_OFFSET -> "INVALID_OFFSET"
                BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "REQUEST_NOT_SUPPORTED"
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "WRITE_NOT_PERMITTED"
                else -> status.toString()
            }
        }($status)"
    }

    private fun isSuccess(status: Int) = status == BluetoothGatt.GATT_SUCCESS

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        val state = ConnectState.from(newState)
        log.log { "onConnectionStateChange: ${fmtStatus(status)}, $state" }
        onStateChange.notifyUpdate { it.invoke(isSuccess(status), state) }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        log.log { "onServicesDiscovered: ${fmtStatus(status)}" }
        onServiceDiscover.notifyUpdate { it.invoke(isSuccess(status), gatt) }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        log.log { "onMtuChanged: ${fmtStatus(status)}, mtu:$mtu" }
        onMtuChange.notifyUpdate { it.invoke(isSuccess(status), mtu) }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        log.log { "onDescriptorRead: ${fmtStatus(status)}" }
        onDescriptorRead.notifyUpdate { it.invoke(isSuccess(status), descriptor) }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        log.log { "onDescriptorWrite: ${fmtStatus(status)}" }
        onDescriptorWrite.notifyUpdate { it.invoke(isSuccess(status), descriptor) }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        log.log { "onCharacteristicRead: ${fmtStatus(status)}" }
        onCharacteristicRead.notifyUpdate { it.invoke(isSuccess(status), characteristic) }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        log.log { "onCharacteristicWrite: ${fmtStatus(status)}" }
        onCharacteristicWrite.notifyUpdate { it.invoke(isSuccess(status), characteristic) }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        log.log { "onCharacteristicChanged" }
        onCharacteristicChanged.notifyUpdate { it.invoke(characteristic) }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        log.log { "onReadRemoteRssi: ${fmtStatus(status)}, rssi:${rssi}" }
        onReadRemoteRssi.notifyUpdate { it.invoke(isSuccess(status), rssi) }
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
    }
}

typealias OnStateChange = (isSuccess: Boolean, state: ConnectState) -> Unit
typealias OnServicesDiscovered = (isSuccess: Boolean, gatt: BluetoothGatt?) -> Unit
typealias  OnMtuChanged = (isSuccess: Boolean, mtu: Int) -> Unit
typealias OnDescriptorRead = (isSuccess: Boolean, descriptor: BluetoothGattDescriptor?) -> Unit
typealias OnDescriptorWrite = (isSuccess: Boolean, descriptor: BluetoothGattDescriptor?) -> Unit
typealias OnCharacteristicRead = (isSuccess: Boolean, characteristic: BluetoothGattCharacteristic?) -> Unit
typealias OnCharacteristicWrite = (isSuccess: Boolean, characteristic: BluetoothGattCharacteristic?) -> Unit
typealias OnCharacteristicChanged = (characteristic: BluetoothGattCharacteristic?) -> Unit
typealias OnReadRemoteRssi = (isSuccess: Boolean, rssi: Int) -> Unit