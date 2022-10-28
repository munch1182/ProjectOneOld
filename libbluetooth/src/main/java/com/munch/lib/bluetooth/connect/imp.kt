package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import com.munch.lib.android.AppHelper
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.env.BluetoothEnv
import com.munch.lib.bluetooth.env.IBluetoothManager
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import com.munch.lib.bluetooth.helper.find
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import android.bluetooth.BluetoothGatt as GATT
import android.bluetooth.BluetoothGattCallback as GATTCallback
import com.munch.lib.bluetooth.connect.BluetoothConnectFailReason as REASON

/**
 * Create by munch1182 on 2022/10/27 18:02.
 */

internal class BluetoothConnector(
    private val mac: String,
    private var dev: BluetoothDevice? = null
) : IBluetoothManager by BluetoothEnv,
    IBluetoothHelperEnv by BluetoothHelperEnv {

    private var gattHelper: GATTHelper? = null

    val state: BluetoothConnectState
        get() = gattHelper?.state ?: BluetoothConnectState.Disconnect

    suspend fun connect(timeout: Long): BluetoothConnectResult {
        return suspendCancellableCoroutine { continuation ->
            launch {
                if (dev == null) {
                    log.log("[$mac]: start FIND dev.")
                    val findDev = BluetoothHelper.find(mac, timeout)
                    if (findDev == null) {
                        log.log("[$mac]: find fail.")
                        continuation.resume(REASON.NotFindDev.to())
                    } else {
                        log.log("[$mac]: find success.")
                        dev = findDev.dev
                    }
                }
                val dev = dev
                if (dev == null) {
                    continuation.resume(REASON.NotFindDev.to())
                } else {
                    val helper = GATTHelper(dev)
                    val result = helper.connect(timeout)
                    gattHelper = if (result.isSuccess) helper else null
                    continuation.resume(result)
                }
            }
        }
    }

    suspend fun disconnect(removeBond: Boolean): Boolean {
        return true
    }

    fun addConnectListener(l: OnBluetoothConnectListener) {
        gattHelper
    }

    fun removeConnectListener(l: OnBluetoothConnectListener) {
    }

    override fun log(content: String) {
        log.log("[$mac]: $content.")
    }
}

/**
 * 实际对系统连接的实现类
 */
internal class GATTHelper(private val sysDev: BluetoothDevice) :
    IBluetoothHelperEnv by BluetoothHelperEnv,
    ARSHelper<OnBluetoothConnectListener>() {

    private val lock = Mutex()
    private var gatt: GATT? = null
    private val callback by lazy { object : GATTCallbackWrapper(sysDev.address) {} }
    private var connectState: BluetoothConnectState = BluetoothConnectState.Disconnect
        get() = runBlocking { lock.withLock { field } }
        set(value) {
            val last = field
            runBlocking { lock.withLock { field = value } }
            log("Connect State: $last -> $value.")
            if (last.isConnecting) {
                if (value.isConnected) {
                    update { it.onConnect(true) }
                } else if (value.isDisconnect) {
                    update { it.onConnect(false) }
                }
            }
        }
    val state: BluetoothConnectState
        get() = connectState

    suspend fun connect(timeout: Long) = suspendCancellableCoroutine { continuation ->
        launch {
            try {
                withTimeout(timeout) {
                    callback.add(object : GATTCallback() {
                        override fun onConnectionStateChange(
                            gatt: GATT?,
                            status: Int,
                            newState: Int
                        ) {
                            super.onConnectionStateChange(gatt, status, newState)
                            callback.remove(this) // todo 监听状态更改
                            if (status != GATT.GATT_SUCCESS || newState != GATT.STATE_CONNECTED) {
                                val result = REASON.SysErr(status).to()
                                log("connect fail: $result.")
                                continuation.resume(result)
                            } else {
                                log("connect success.")
                                continuation.resume(BluetoothConnectResult.Success)
                            }
                        }
                    })
                    log("start CONNECT with timeout $timeout ms.")
                    gatt = sysDev.connectGatt(AppHelper, false, callback)
                }
            } catch (_: Exception) {
                log("connect timeout.")
                continuation.resume(REASON.ConnectTimeout.to())
            }
        }
    }

    override fun log(content: String) {
        log.log("[${sysDev.address}]: $content")
    }
}

open class GATTCallbackWrapper(
    private val mac: String,
    private var wrap: MutableList<GATTCallback>? = null
) : GATTCallback(), IBluetoothHelperEnv by BluetoothHelperEnv {

    fun add(vararg callbacks: GATTCallback): GATTCallbackWrapper {
        if (wrap == null) {
            wrap = mutableListOf(*callbacks)
        } else {
            wrap?.addAll(callbacks)
        }
        return this
    }

    fun remove(callback: GATTCallback): GATTCallbackWrapper {
        wrap?.remove(callback)
        return this
    }

    override fun log(content: String) {
        log.log("[$mac]: $content")
    }

    override fun onConnectionStateChange(gatt: GATT?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        log("onConnectionStateChange: status: ${status.status()}, newState: ${newState.state()}, gatt: ${gatt.str()}.")
        wrap?.forEach { it.onConnectionStateChange(gatt, status, newState) }
    }

    override fun onServicesDiscovered(gatt: GATT?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        log("onServicesDiscovered: status: ${status.status()}, gatt: ${gatt.str()}.")
        wrap?.forEach { it.onServicesDiscovered(gatt, status) }
    }

    override fun onMtuChanged(gatt: GATT?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        log("onMtuChanged: mtu: ${mtu}, status: ${status.status()}, gatt: ${gatt.str()}.")
        wrap?.forEach { it.onMtuChanged(gatt, mtu, status) }
    }

    override fun onDescriptorRead(
        gatt: GATT?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        log("onDescriptorRead: status: ${status.status()}, descriptor: ${descriptor.str()}.")
        wrap?.forEach { it.onDescriptorRead(gatt, descriptor, status) }
    }

    override fun onDescriptorWrite(
        gatt: GATT?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        log("onDescriptorWrite: status: ${status.status()}, descriptor: ${descriptor.str()}.")
        wrap?.forEach { it.onDescriptorWrite(gatt, descriptor, status) }
    }

    override fun onCharacteristicRead(
        gatt: GATT?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        log("onCharacteristicRead: status: ${status.status()}, characteristic: ${characteristic.str()}.")
        wrap?.forEach { it.onCharacteristicRead(gatt, characteristic, status) }
    }

    override fun onCharacteristicWrite(
        gatt: GATT?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        log("onCharacteristicWrite: status: ${status.status()}, characteristic: ${characteristic.str()}.")
        wrap?.forEach { it.onCharacteristicWrite(gatt, characteristic, status) }
    }

    override fun onCharacteristicChanged(
        gatt: GATT?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        log("onCharacteristicChanged: characteristic: ${characteristic.str()}.")
        wrap?.forEach { it.onCharacteristicChanged(gatt, characteristic) }
    }

    override fun onPhyRead(gatt: GATT?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        log("onPhyRead: rxPhy: $txPhy, $rxPhy: $rxPhy, status: ${status.status()}, gatt: ${gatt.str()}.")
        wrap?.forEach { it.onPhyRead(gatt, txPhy, rxPhy, status) }
    }

    override fun onPhyUpdate(gatt: GATT?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        log("onPhyUpdate: rxPhy: $txPhy, $rxPhy: $rxPhy, status: ${status.status()}, gatt: ${gatt.str()}.")
        wrap?.forEach { it.onPhyUpdate(gatt, txPhy, rxPhy, status) }
    }

    override fun onReadRemoteRssi(gatt: GATT?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        log("onReadRemoteRssi: rssi: $rssi, status: ${status.status()}, gatt: ${gatt.str()}.")
        wrap?.forEach { it.onReadRemoteRssi(gatt, rssi, status) }
    }

    override fun onServiceChanged(gatt: GATT) {
        super.onServiceChanged(gatt)
        log("onServiceChanged: gatt: ${gatt.str()}")
        wrap?.forEach { it.onServiceChanged(gatt) }
    }

    private fun Int.status() = when (this) {
        GATT.GATT_READ_NOT_PERMITTED -> "GATT_READ_NOT_PERMITTED"
        GATT.GATT_WRITE_NOT_PERMITTED -> "GATT_WRITE_NOT_PERMITTED"
        GATT.GATT_INSUFFICIENT_AUTHENTICATION -> "GATT_INSUFFICIENT_AUTHENTICATION"
        GATT.GATT_REQUEST_NOT_SUPPORTED -> "GATT_REQUEST_NOT_SUPPORTED"
        GATT.GATT_INSUFFICIENT_ENCRYPTION -> "GATT_INSUFFICIENT_ENCRYPTION"
        GATT.GATT_INVALID_OFFSET -> "GATT_INVALID_OFFSET"
        GATT.GATT_INVALID_ATTRIBUTE_LENGTH -> "GATT_INVALID_ATTRIBUTE_LENGTH"
        GATT.GATT_CONNECTION_CONGESTED -> "GATT_CONNECTION_CONGESTED"
        else -> toString()
    }

    private fun Int.state() = when (this) {
        BluetoothProfile.STATE_CONNECTED -> "CONNECT"
        BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECT"
        BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
        BluetoothProfile.STATE_DISCONNECTING -> "DISCONNECTING"
        else -> toString()
    }

    private fun GATT?.str() =
        if (this == null) "null" else toString().replace("android.bluetooth.BluetoothGatt", "")

    private fun BluetoothGattDescriptor?.str() =
        if (this == null) "null" else toString().replace(
            "android.bluetooth.BluetoothGattDescriptor", ""
        )

    private fun BluetoothGattCharacteristic?.str() =
        if (this == null) "null" else toString().replace(
            "android.bluetooth.BluetoothGattCharacteristic", ""
        )

}
