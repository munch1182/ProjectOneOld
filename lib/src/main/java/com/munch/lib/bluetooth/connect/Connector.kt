package com.munch.lib.bluetooth.connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.os.Build
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.data.BluetoothDataHelper
import com.munch.lib.bluetooth.data.IData
import com.munch.lib.bluetooth.data.OnByteArrayReceived
import com.munch.lib.task.ThreadHandler

/**
 * Create by munch1182 on 2021/12/7 09:41.
 */
class Connector(
    val dev: BluetoothDev,
    private var connectSet: BleConnectSet? = null,
    //gatt连接回调到handler所在线程
    private val handler: ThreadHandler
) : IConnect, IData {

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
                connectStateChange?.invoke(dev, old, value)
            }
        }
    private val defaultConnectSet by lazy { BleConnectSet() }
    private val set: BleConnectSet
        get() = connectSet ?: defaultConnectSet

    val state: ConnectState
        get() = currentState
    private var connectStateChange: OnConnectStateChange? = null
    private var dataHelper: BluetoothDataHelper? = null

    private val timeout by lazy {
        Runnable {
            if (currentState != ConnectState.CONNECTING) {
                return@Runnable
            }
            connectFail(ConnectFail.Timeout(set.timeout))
        }
    }
    internal val gattWrapper = object : GattWrapper(dev.mac, logSystem) {

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
                                dataHelper = BluetoothDataHelper(this@Connector)
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
            logHelper.withEnable { "${dev.mac}: start connect." }
            currentState = ConnectState.CONNECTING
            connectListener?.onConnectStart(dev)
        }

        override fun onConnected(dev: BluetoothDev) {
            currentState = ConnectState.CONNECTED
            logHelper.withEnable { "${dev.mac}: connected." }
            connectListener?.onConnected(dev)
        }

        override fun onConnectFail(dev: BluetoothDev, fail: ConnectFail) {
            super.onConnectFail(dev, fail)
            //不能循环调用
            if (fail !is ConnectFail.CancelByUser) {
                //不在此处更改currentState的状态，而是在disconnect()中更改
                logHelper.withEnable { "${dev.mac}: connect fail: $fail." }
                if (fail is ConnectFail.SystemError) {
                    disconnect(DisconnectCause.BySystem(fail.status))
                } else {
                    disconnect(DisconnectCause.ByHelper)
                }
            }
            connectListener?.onConnectFail(dev, fail)
        }
    }
    private var connectListener: OnConnectListener? = null

    fun setConnectListener(listener: OnConnectListener): Connector {
        connectListener = listener
        return this
    }

    fun setOnConnectStateChangeListener(listener: OnConnectStateChange?): Connector {
        connectStateChange = listener
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

        //todo 是否要为每一个gatt连接分配一个handler，因为数据的发送回调和接收回调会回调到该线程，
        //todo 或者进行接收后的分发
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
        handler.removeCallbacks(timeout)
        if (currentState != ConnectState.CONNECTED && currentState != ConnectState.CONNECTING) {
            return
        }
        logHelper.withEnable { "${dev.mac}: disconnect: $cause." }
        //因为不会触发回调
        currentState = ConnectState.DISCONNECTING
        disconnectOnly()
        closeGatt()
        currentState = ConnectState.DISCONNECTED
        connectFail(ConnectFail.CancelByUser)
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
        connectCallback.onConnectFail(dev, cause)
    }

    private fun imp(imp: () -> Unit) {
        if (Thread.currentThread().id != handler.thread.id) {
            handler.post(imp)
        } else {
            imp.invoke()
        }
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun send(byteArray: ByteArray, timeout: Long): Boolean {
        return dataHelper?.send(byteArray, timeout) ?: false
    }

    override fun onReceived(received: OnByteArrayReceived) {
        dataHelper?.onReceived(received)
    }
}

typealias OnConnectStateChange = (dev: BluetoothDev, old: ConnectState, now: ConnectState) -> Unit