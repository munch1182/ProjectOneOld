package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.os.Build
import com.munch.lib.base.Cancelable

/**
 * Create by munch1182 on 2021/8/25 16:47.
 */
interface Connector : Cancelable {

    val device: BtDevice

    fun connect()

    fun disconnect()

    override fun cancel() {
        disconnect()
    }
}

interface OnConnectListener {

    fun onStart()
    fun onConnectSuccess()
    fun onConnectFail()
}

class ClassicConnector(override val device: BtDevice) : Connector {
    override fun connect() {
    }

    override fun disconnect() {
    }
}

class BleConnector(override val device: BtDevice) : Connector {

    private val logHelper = BluetoothHelper.logHelper
    private val logSystem = BluetoothHelper.logSystem
    private var gatt: BluetoothGatt? = null
    internal var connectListener: OnConnectListener? = null

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            logSystem.withEnable { "onConnectionStateChange: newState: $newState, states:$status" }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                connectListener?.onConnectFail()
                removeConnectListener()
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED/*2*/) {
                this@BleConnector.gatt = gatt
                //onServicesDiscovered
                gatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING/*3*/) {
                this@BleConnector.gatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            val gattService = gatt?.services
            logSystem.withEnable { "onServicesDiscovered: service: ${gattService?.size ?: "null"}, states:$status" }
            connectListener?.onConnectSuccess()
            removeConnectListener()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            device.device.connectGatt(
                null, false, callback, BluetoothDevice.TRANSPORT_AUTO,
                BluetoothDevice.PHY_LE_1M_MASK, BluetoothHelper.instance.workHandler
            )
        } else {
            device.device.connectGatt(null, false, callback)
        }
    }

    override fun disconnect() {
        gatt?.disconnect()
    }
}