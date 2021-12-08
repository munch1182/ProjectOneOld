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
        set(value) = synchronized(lock) {
            if (value != field) {
                val old = field
                field = value
                logHelper.withEnable { "${dev.mac}: connect state: $old -> $value." }
            }
        }
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
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        if (currentState == ConnectState.CONNECTING) {
                            if (status == 133) {
                                connectFail(ConnectFail.Code133Error())
                            } else {
                                connectFail(ConnectFail.SystemError(status))
                            }
                        } else {
                            disconnect(DisconnectCause.BySystem(status))
                        }
                    }
                    BluetoothAdapter.STATE_CONNECTED -> {
                        if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                            connectFail(ConnectFail.SystemError(status))
                        } else {
                            post {
                                val fail = set.onConnectSet?.onConnectSet(this)
                                if (fail != null) {
                                    connectFail(fail)
                                    return@post
                                }
                                val complete = set.onConnectComplete?.onConnectComplete(dev) ?: true
                                if (!complete) {
                                    connectFail(ConnectFail.DisallowConnect("onConnectComplete"))
                                    return@post
                                }
                                connectSuccess()
                            }
                        }
                    }
                    BluetoothAdapter.STATE_DISCONNECTING -> {
                        currentState = ConnectState.DISCONNECTING
                    }
                    BluetoothAdapter.STATE_CONNECTING -> currentState = ConnectState.CONNECTING
                }
            }
        }
    }

    private val connectCallback = object : OnConnectListener {

        override fun onConnectStart(dev: BluetoothDev) {
            super.onConnectStart(dev)
            currentState = ConnectState.CONNECTING
            logHelper.withEnable { "${dev.mac}: start connect." }
            connectListener?.onConnectStart(dev)
        }

        override fun onConnected(dev: BluetoothDev) {
            currentState = ConnectState.CONNECTED
            logHelper.withEnable { "${dev.mac}: connected." }
            connectListener?.onConnected(dev)
        }

        override fun onConnectFail(dev: BluetoothDev, fail: ConnectFail) {
            super.onConnectFail(dev, fail)
            //不在此处更改currentState的状态，而是在disconnect()中更改
            logHelper.withEnable { "${dev.mac}: connect fail: $fail." }
            if (fail is ConnectFail.SystemError) {
                disconnect(DisconnectCause.BySystem(fail.status))
            } else {
                disconnect(DisconnectCause.ByHelper)
            }
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
                dev, ConnectFail.DisallowConnect("currentConnectState: $currentState")
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
        disconnect(DisconnectCause.ByUser)
    }

    private fun disconnect(cause: DisconnectCause) {
        if (currentState != ConnectState.CONNECTED && currentState != ConnectState.CONNECTING) {
            return
        }
        logHelper.withEnable { "disconnect: $cause." }
        //因为不会触发回调
        currentState = ConnectState.DISCONNECTING
        disconnectOnly()
        closeGatt()
        currentState = ConnectState.DISCONNECTED
    }


    @SuppressLint("MissingPermission")
    private fun disconnectOnly() {
        gatt?.disconnect()
    }

    internal fun connectSuccess() {
        handler.removeCallbacks(timeout)
        connectCallback.onConnected(dev)
    }

    internal fun connectFail(cause: ConnectFail) {
        handler.removeCallbacks(timeout)
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