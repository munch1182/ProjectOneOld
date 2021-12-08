package com.munch.lib.bluetooth.connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.os.Build
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.task.ThreadHandler

/**
 * Create by munch1182 on 2021/12/7 09:41.
 */
@SuppressLint("MissingPermission")
class Connector(
    private val dev: BluetoothDev,
    private var connectSet: BleConnectSet? = null,
    //gatt连接回调到handler所在线程
    private val handler: ThreadHandler
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

    private val timeout by lazy {
        Runnable {
            if (currentState != ConnectState.CONNECTING) {
                return@Runnable
            }
            connectFail(ConnectFail.Timeout(set.timeout))
        }
    }
    private val gattWrapper = object : GattWrapper(dev.mac, logSystem) {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            imp {
                when (newState) {
                    BluetoothAdapter.STATE_DISCONNECTING -> {
                        currentState = ConnectState.DISCONNECTING
                    }
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        if (currentState == ConnectState.CONNECTING) {
                            if (status == 133) {
                                connectFail(ConnectFail.Code133Error())
                            } else {
                                connectFail(ConnectFail.SystemError(status))
                            }
                        }
                        currentState = ConnectState.DISCONNECTED
                        closeGatt()
                    }
                    BluetoothAdapter.STATE_CONNECTED -> {
                        if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                            connectFail(ConnectFail.SystemError(status))
                        } else {
                            postDelay({
                                val fail = set.onConnectSet?.onConnectSet(this)
                                if (fail != null) {
                                    connectFail(fail)
                                    return@postDelay
                                }
                                val complete = set.onConnectComplete?.onConnectComplete(dev) ?: true
                                if (!complete) {
                                    connectFail(ConnectFail.DisallowConnected("onConnectComplete"))
                                    return@postDelay
                                }
                                connectSuccess()
                            }, 2000L)
                        }
                    }
                }
            }
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
                dev, ConnectFail.DisallowConnected("currentConnectState: $currentState")
            )
            return
        }
        connectCallback.onConnectStart(dev)
        closeGatt()
        handler.postDelayed(timeout, set.timeout)
        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dev.dev?.connectGatt(
                null, false, gattWrapper.callback, set.transport, set.phy, handler
            )
        } else {
            dev.dev?.connectGatt(null, false, gattWrapper.callback, set.transport)
        }
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        gatt?.close()
        gatt = null
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        //蓝牙未关闭情形下，调用此方法去触发断开回调
        disconnectOnly()
        //todo 蓝牙已断开的情形
        closeGatt()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    private fun disconnectOnly() {
        gatt?.disconnect()
    }

    internal fun connectSuccess() {
        handler.removeCallbacks(timeout)
        logHelper.withEnable { "connectSuccess." }
        connectCallback.onConnected(dev)
    }

    internal fun connectFail(cause: ConnectFail) {
        handler.removeCallbacks(timeout)
        logHelper.withEnable { "connectFail: $cause." }
        closeGatt()
        connectCallback.onConnectFail(dev, cause)
    }

    private fun imp(imp: () -> Unit) {
        if (Thread.currentThread().id != handler.thread.id) {
            handler.post(imp)
        } else {
            imp.invoke()
        }
    }

}