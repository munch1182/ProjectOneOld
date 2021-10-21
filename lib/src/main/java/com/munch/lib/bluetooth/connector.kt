package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.os.Build
import com.munch.lib.base.Manageable

/**
 * Create by munch1182 on 2021/8/25 16:47.
 */
interface Connector : Manageable {

    val device: BluetoothDev

    fun connect()

    fun disconnect()

    override fun cancel() {
        disconnect()
    }
}

/**
 * 连接动作的回调，即开始连接-连接成功/连接失败
 *
 * 如果要监听连接断开，使用[BluetoothStateHelper]
 */
interface OnConnectListener {

    fun onStart()
    fun onConnectSuccess()
    fun onConnectFail(status: Int)
}

class ClassicConnector(override val device: BluetoothDev) : Connector {
    override fun connect() {
    }

    override fun disconnect() {
    }

    override fun destroy() {

    }
}

class BleConnector(override val device: BluetoothDev) : Connector {

    private val logHelper = BluetoothHelper.logHelper
    private val logSystem = BluetoothHelper.logSystem
    private var gatt: BluetoothGatt? = null
    var connectListener: OnConnectListener? = null
    var connectByAllIfCan = true

    private val gattCallback = GattCallback()

    /**
     * 当一次连接结束后(成功/失败)，则移除监听，因为连接是个一次性的动作
     * 连接的状态不在这个回调中，这个只表示连接的动作
     */
    private fun removeConnectListener() {
        connectListener = null
    }

    override fun connect() {
        connectListener?.onStart()
        //实际上gatt关闭时已被设置为null
        if (!reconnect()) {
            logHelper.withEnable { "bleConnector connect:${device.mac} by device" }
            if (connectByAllIfCan) connectIfCan() else connectJust()
        }
    }

    private fun connectIfCan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instance = BluetoothHelper.instance
            val le2M = instance.set.isLe2MPhySupported
            val mask = if (le2M) BluetoothDevice.PHY_LE_2M_MASK else BluetoothDevice.PHY_LE_1M_MASK
            if (le2M) {
                logHelper.withEnable { "bleConnector connect: 2M_MASK" }
            }
            gatt = device.dev.connectGatt(
                instance.context, false, gattCallback,
                BluetoothDevice.TRANSPORT_LE, mask
            )
        } else {
            connectJust()
        }
    }

    private fun connectJust() {
        gatt = device.dev.connectGatt(null, false, gattCallback)
    }

    override fun disconnect() {
        logHelper.withEnable { "bleConnector disconnect:${gatt?.device?.address}" }
        gatt?.disconnect()
        //连接中断开主动触发
        if (BluetoothHelper.instance.state.isConnecting) {
            BluetoothHelper.instance.newState(BluetoothState.IDLE)
        }
    }

    override fun destroy() {
        logHelper.withEnable { "destroy bleConnector" }
        disconnect()
        closeGatt()
        connectListener = null
    }

    private fun closeGatt() {
        logHelper.withEnable { "bleConnector closeGatt" }
        gatt?.disconnect()
        //调用此方法后不会再触发任何系统回调
        gatt?.close()
        gatt = null
        //因此手动触发
        BluetoothHelper.instance.newState(BluetoothState.IDLE)
    }

    /**
     * 当调用[connect]后且未调用[disconnect]，即[gatt]不为null时(如蓝牙超出范围断开)，可直接调用此方法来重新连接
     */
    private fun reconnect(): Boolean {
        logHelper.withEnable { "bleConnector reconnect:${gatt?.device?.address}" }
        return gatt?.connect() ?: false
    }

    private inner class GattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            logSystem.withEnable {
                val statusStr = statusStr(status)
                "onConnectionStateChange(${gatt?.device?.address}): newState: $newState, status:$statusStr"
            }

            val instance = BluetoothHelper.instance
            //连接成功
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                if (gatt != null) {
                    //onServicesDiscovered
                    if (gatt.discoverServices()) {
                        //发现服务成功
                        return
                    }
                    logSystem.withEnable { "discoverServices(${gatt.device?.address}): fail" }
                }
            }
            //除了成功，其它情形都应该关闭连接并更新状态
            //包括意外断开连接，以及连接时发现服务失败
            closeGatt()
            if (instance.state.isConnecting) {
                connectFail(status)
            }
        }

        private fun statusStr(status: Int): String {
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

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            val gattService = gatt?.services
            logSystem.withEnable {
                val statusStr = statusStr(status)
                "onServicesDiscovered: service: ${gattService?.size ?: "null"}, states:$statusStr"
            }
            connectSuccess()
        }

        private fun connectSuccess() {
            connectListener?.onConnectSuccess()
            removeConnectListener()
        }

        private fun connectFail(status: Int) {
            connectListener?.onConnectFail(status)
            removeConnectListener()
        }

        //gatt.readRemoteRssi()
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                device.rssi = rssi
            }
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
            logSystem.withEnable { "onPhyRead: txPhy: ${txPhy}, rxPhy: ${rxPhy}, states:$status" }
        }

        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            logSystem.withEnable { "onPhyUpdate: txPhy: ${txPhy}, rxPhy: ${rxPhy}, states:$status" }
        }
    }
}