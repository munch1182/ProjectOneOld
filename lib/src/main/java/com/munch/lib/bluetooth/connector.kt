package com.munch.lib.bluetooth

import android.bluetooth.*
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
    fun onConnectFail()
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
    internal var connectListener: OnConnectListener? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            logSystem.withEnable { "onConnectionStateChange(${gatt?.device?.address}): newState: $newState, states:$status" }

            if (newState == BluetoothProfile.STATE_CONNECTED/*2*/) {
                if (status != BluetoothGatt.GATT_SUCCESS || gatt == null) {
                    connectListener?.onConnectFail()
                    removeConnectListener()
                    return
                }
                this@BleConnector.gatt = gatt
                //onServicesDiscovered
                val discoverServices = gatt.discoverServices()
                if (!discoverServices) {
                    logSystem.withEnable { "discoverServices(${gatt.device?.address}): fail" }
                    return
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED/*0*/) {
                BluetoothHelper.instance.newState(BluetoothState.IDLE)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            val gattService = gatt?.services
            logSystem.withEnable { "onServicesDiscovered: service: ${gattService?.size ?: "null"}, states:$status" }
            connectSuccess()
        }

        private fun connectSuccess() {
            connectListener?.onConnectSuccess()
            removeConnectListener()
        }

        //gatt.readRemoteRssi()
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                device.rssi = rssi
            }
        }
    }

    /**
     * 当一次连接结束后(成功/失败)，则移除监听，因为连接是个一次性的动作
     * 连接的状态不在这个回调中，这个只表示连接的动作
     */
    private fun removeConnectListener() {
        connectListener = null
    }

    override fun connect() {
        connectListener?.onStart()
        if (gatt != null) {
            reconnect()
        } else {
            logHelper.withEnable { "bleConnector connect:${device.mac}" }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                device.dev.connectGatt(
                    null, false, gattCallback, BluetoothDevice.TRANSPORT_AUTO,
                    BluetoothDevice.PHY_LE_1M_MASK, BluetoothHelper.instance.workHandler
                )
            } else {
                device.dev.connectGatt(null, false, gattCallback)
            }
        }
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
        //调用此方法后不会再触发任何系统回调
        gatt?.close()
        //因此手动触发
        BluetoothHelper.instance.newState(BluetoothState.IDLE)
        gatt = null
        connectListener = null
    }

    /**
     * 当调用[connect]后且未调用[disconnect]，即[gatt]不为null时(如蓝牙超出范围断开)，可直接调用此方法来重新连接
     */
    private fun reconnect() {
        logHelper.withEnable { "bleConnector reconnect:${gatt?.device?.address}" }
        gatt?.connect()
    }
}