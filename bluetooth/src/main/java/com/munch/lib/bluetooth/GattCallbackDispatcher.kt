package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import com.munch.lib.extend.suspendCancellableCoroutine
import com.munch.lib.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
open class GattCallbackDispatcher(private val log: Logger) : BluetoothGattCallback() {

    var gatt: BluetoothGatt? = null
        get() {
            if (field == null) {
                log.log { "gatt null." }
            }
            return field
        }

    private var _onStateChange: OnStateChange? = null
    private var _onServiceDiscover: OnServicesDiscovered? = null
    private var _onMtuChange: OnMtuChanged? = null
    private var _onDescriptorRead: OnDescriptorRead? = null
    private var _onDescriptorWrite: OnDescriptorWrite? = null
    private var _onCharacteristicRead: OnCharacteristicRead? = null
    private var _onCharacteristicWrite: OnCharacteristicWrite? = null
    private var _onCharacteristicChanged: OnCharacteristicChanged? = null
    private var _onReadRemoteRssi: OnReadRemoteRssi? = null

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

    private fun mac(gatt: BluetoothGatt?) = gatt?.device?.address

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        this.gatt = gatt
        val state = ConnectState.from(newState)
        log.log { "[${mac(gatt)}] onConnectionStateChange: ${fmtStatus(status)}, $state." }
        val success = isSuccess(status)
        _onStateChange?.invoke(success, state)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        log.log { "[${mac(gatt)}] onServicesDiscovered: ${fmtStatus(status)}." }
        val success = isSuccess(status)
        _onServiceDiscover?.invoke(success, gatt)
    }

    /**
     * 用于发现服务
     * 如果超时未发现或者发现失败，则返回为null，否则则是发现成功
     */
    suspend fun discoverService(timeout: Long = 1000L): BluetoothGatt? {
        val g = gatt ?: return null
        if (g.services.isNotEmpty()) {
            log.log { "[${mac(gatt)}] services had discovered: ${g.services.size}." }
            return gatt
        }
        val serviceGatt = suspendCancellableCoroutine<BluetoothGatt?>(timeout) {
            _onServiceDiscover = { isSuccess, gatt -> it.resume(if (isSuccess) gatt else null) }
            val dis = runBlocking(Dispatchers.Main) {
                g.discoverServices()
                    .also { log.log { "[${mac(gatt)}] DISCOVER SERVICES(${it})." } }
            }
            if (!dis) {
                it.resume(null)
            }
        }
        _onServiceDiscover = null
        log.log { "[${mac(gatt)}] services discovered: ${serviceGatt?.services?.size}." }
        return serviceGatt
    }

    /**
     * @see discoverService
     */
    fun getService(uuid: UUID): BluetoothGattService? {
        val g = gatt ?: return null
        return g.getService(uuid)
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        log.log { "[${mac(gatt)}] onMtuChanged: ${fmtStatus(status)}, mtu:$mtu." }
        val success = isSuccess(status)
        _onMtuChange?.invoke(success, mtu)
    }

    /**
     * 协商mtu值，返回回调的mtu值
     *
     * mtu的回调可能先于请求和回调之前
     */
    suspend fun requestMtu(mtu: Int = 247, timeout: Long = 10000L): Int? {
        val g = gatt ?: return null
        val mtuChanged = suspendCancellableCoroutine<Int?>(timeout) {
            _onMtuChange = { isSuccess, mtu -> it.resume(if (isSuccess) mtu else null) }
            val request = runBlocking {
                g.requestMtu(mtu).also { log.log { "[${mac(gatt)}] REQUEST MTU(${mtu}): $it." } }
            }
            if (!request) {
                it.resume(null)
            }
        }
        _onMtuChange = null
        log.log { "[${mac(gatt)}] mtu requested: ${mtuChanged}." }
        return mtuChanged
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        log.log { "[${mac(gatt)}] onDescriptorRead: ${fmtStatus(status)}" }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        log.log { "[${mac(gatt)}] onDescriptorWrite: ${fmtStatus(status)}" }
    }

    suspend fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic?,
        timeout: Long = 10000L
    ): BluetoothGattCharacteristic? {
        val g = gatt ?: return null
        val c = characteristic ?: return null
        val readBluetoothGattCharacteristic =
            suspendCancellableCoroutine<BluetoothGattCharacteristic?>(timeout) {
                _onCharacteristicRead = { isSuccess, characteristic ->
                    it.resume(if (isSuccess) characteristic else null)
                }
                val read = runBlocking(Dispatchers.Main) {
                    g.readCharacteristic(c)
                        .also { log.log { "[${mac(gatt)}] READ CHARACTERISTIC(${c.uuid}: ${it})." } }
                }
                if (!read) {
                    it.resume(null)
                }
            }
        return readBluetoothGattCharacteristic
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        log.log { "[${mac(gatt)}] onCharacteristicRead(${characteristic?.uuid}): ${fmtStatus(status)}" }
        _onCharacteristicRead?.invoke(isSuccess(status), characteristic)
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        log.log { "[${mac(gatt)}] onCharacteristicWrite: ${fmtStatus(status)}" }
    }

    suspend fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic?,
        enable: Boolean,
        timeout: Long = 3000L
    ): BluetoothGattCharacteristic? {
        val g = gatt ?: return null
        val c = characteristic ?: return null
        val updateCharacteristic =
            suspendCancellableCoroutine<BluetoothGattCharacteristic?>(timeout = timeout) {
                _onCharacteristicChanged = { c2 ->
                    if (c.uuid == c2?.uuid) {
                        it.resume(c2)
                    }
                }
                val setEnable =
                    runBlocking(Dispatchers.Main) { g.setCharacteristicNotification(c, enable) }
                if (!setEnable) {
                    it.resume(null)
                }
            }
        return updateCharacteristic
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        log.log { "[${mac(gatt)}] onCharacteristicChanged" }
        _onCharacteristicChanged?.invoke(characteristic)
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        log.log { "[${mac(gatt)}] onReadRemoteRssi: ${fmtStatus(status)}, rssi:${rssi}" }
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
typealias OnMtuChanged = (isSuccess: Boolean, mtu: Int) -> Unit
typealias OnDescriptorRead = (isSuccess: Boolean, descriptor: BluetoothGattDescriptor?) -> Unit
typealias OnDescriptorWrite = (isSuccess: Boolean, descriptor: BluetoothGattDescriptor?) -> Unit
typealias OnCharacteristicRead = (isSuccess: Boolean, characteristic: BluetoothGattCharacteristic?) -> Unit
typealias OnCharacteristicWrite = (isSuccess: Boolean, characteristic: BluetoothGattCharacteristic?) -> Unit
typealias OnCharacteristicChanged = (characteristic: BluetoothGattCharacteristic?) -> Unit
typealias OnReadRemoteRssi = (isSuccess: Boolean, rssi: Int) -> Unit