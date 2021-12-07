package com.munch.lib.bluetooth.connect

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Build
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.task.ThreadHandler

/**
 * Create by munch1182 on 2021/12/7 09:41.
 */
class Connector(
    private val dev: BluetoothDev,
    private var connectSet: BleConnectSet? = null,
    //gatt连接回调到handler所在线程
    private val handler: ThreadHandler? = null
) : IConnect {

    private val logSystem = BluetoothHelper.logSystem
    private val logHelper = BluetoothHelper.logHelper

    private var gatt: BluetoothGatt? = null

    private val lock = Object()
    private var currentState: ConnectState = ConnectState.DISCONNECTED
        get() = synchronized(lock) { field }
        set(value) = synchronized(lock) { field = value }
    private val defaultConnectSet by lazy { BleConnectSet() }
    private val set: BleConnectSet
        get() = connectSet ?: defaultConnectSet

    val state: ConnectState
        get() = currentState

    private val gattCallback = object : BleGattCallback(logSystem) {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            imp {
                when (newState) {
                    BluetoothAdapter.STATE_DISCONNECTING -> {
                        currentState = ConnectState.DISCONNECTING
                    }
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        currentState = ConnectState.DISCONNECTED
                        closeGatt()
                    }
                    BluetoothAdapter.STATE_CONNECTED -> {
                        if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                            connectFail(ConnectFail.SystemError(status))
                        } else {
                            if (!set.needDiscoverServices) {
                                connectSuccess()
                            } else {
                                val discoverServices = gatt.discoverServices()
                                logHelper.withEnable { "discoverServices: $discoverServices" }
                                if (!discoverServices) {
                                    connectFail(ConnectFail.ServiceDiscoveredFail)
                                }
                                //等待onServicesDiscovered
                            }
                        }
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            imp {
                if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                    connectFail(ConnectFail.ServiceDiscoveredFail)
                    return@imp
                }
                if (set.onServicesHandler?.let { !it.onServicesDiscovered(gatt) } == true) {
                    connectFail(ConnectFail.ServiceDiscoveredFail)
                    return@imp
                }
                requestMtu()
            }
        }

        //todo 同一时间只能等待一个回调，即onServicesDiscovered如果调用了其余等待回调的操作，需要等待回调完成才能requestMtu，如onDescriptorWrite
        @SuppressLint("MissingPermission")
        private fun requestMtu() {
            if (set.maxMTU != 0) {
                val requestMtu = gatt!!.requestMtu(set.maxMTU)
                logHelper.withEnable { "request mtu: ${set.maxMTU}, $requestMtu" }
                if (!requestMtu) {
                    connectFail(ConnectFail.MtuSetFail)
                }
                //等待mtu回调
                return
            }
            connectSuccess()
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                connectFail(ConnectFail.MtuSetFail)
                return
            }
            connectSuccess()
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            requestMtu()
        }
    }

    private val connectCallback = object : OnConnectListener {

        override fun onConnectStart(dev: BluetoothDev) {
            super.onConnectStart(dev)
            currentState = ConnectState.CONNECTING
            connectListener?.onConnectStart(dev)
        }

        override fun onConnected(dev: BluetoothDev) {
            currentState = ConnectState.CONNECTED
            connectListener?.onConnected(dev)
        }

        override fun onConnectFail(dev: BluetoothDev, fail: ConnectFail) {
            super.onConnectFail(dev, fail)
            currentState = ConnectState.DISCONNECTED
            connectListener?.onConnectFail(dev, fail)
        }
    }
    private var connectListener: OnConnectListener? = null

    fun setConnectListener(listener: OnConnectListener): Connector {
        connectListener = listener
        return this
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect() {
        if (!currentState.canConnect) {
            connectCallback.onConnectFail(
                dev,
                ConnectFail.DisallowConnected("currentConnectState: $currentState")
            )
            return
        }
        connectCallback.onConnectStart(dev)
        closeGatt()
        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dev.dev?.connectGatt(
                null, false, gattCallback, set.transport, set.phy, handler
            )
        } else {
            dev.dev?.connectGatt(null, false, gattCallback, set.transport)
        }
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        try {
            gatt?.close()
            gatt = null
        } catch (_: Exception) {
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        //蓝牙未关闭情形下，调用此方法去触发断开回调
        disconnectOnly()
        //todo 蓝牙已断开的情形
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    private fun disconnectOnly() {
        gatt?.disconnect()
    }

    internal fun connectSuccess() {
        logHelper.withEnable { "connectSuccess." }
    }

    internal fun connectFail(cause: ConnectFail) {
        logHelper.withEnable { "connectFail: $cause." }
        closeGatt()
    }

    private fun imp(imp: () -> Unit) {
        if (Thread.currentThread().id != handler?.thread?.id) {
            handler?.post(imp)
        } else {
            imp.invoke()
        }
    }

}