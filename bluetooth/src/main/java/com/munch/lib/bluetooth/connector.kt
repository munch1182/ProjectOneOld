package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.munch.lib.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Created by munch1182 on 2022/5/19 1:52.
 */
sealed class ConnectState {
    object Disconnected : ConnectState() {
        override fun toString() = "DISCONNECTED"
    }

    object Connecting : ConnectState() {
        override fun toString() = "CONNECTING"
    }

    object Connected : ConnectState() {
        override fun toString() = "CONNECTED"
    }

    object Disconnecting : ConnectState() {
        override fun toString() = "DISCONNECTING"
    }

    companion object {

        fun from(gattState: Int): ConnectState {
            return when (gattState) {
                BluetoothGatt.STATE_CONNECTED -> Connected
                BluetoothGatt.STATE_CONNECTING -> Connecting
                BluetoothGatt.STATE_DISCONNECTED -> Disconnected
                BluetoothGatt.STATE_DISCONNECTING -> Disconnecting
                else -> throw IllegalStateException("gattState: $gattState")
            }
        }
    }
}

/**
 * gatt cmd -> callback -> state change -> gatt cmd
 */
class BleConnector(private val dev: BluetoothDev, private val log: Logger) : Connector {

    private var connectListener: ConnectListener? = null
    internal var helper: BluetoothHelper? = BluetoothHelper.instance
    private var gatt: BluetoothGatt? = null

    private var _currState: ConnectState = ConnectState.Disconnected
        get() = synchronized(this) { field }
        set(value) {
            synchronized(this) {
                lastState = field
                field = value
                _curr.postValue(value)
            }
            log.log { "[${dev.mac}] connect state: $lastState -> $field." }
            if (lastState == value) {
                //dev
                log.log { "repeat: $value, $lastState" }
            }
            when (value) {
                ConnectState.Connecting -> {}
                ConnectState.Connected -> helper?.cacheDev(dev)
                ConnectState.Disconnecting -> disconnectBy()
                ConnectState.Disconnected -> {
                    closeIfDisconnected()
                    helper?.clearDev(dev.mac)
                }
            }
        }
    override val currState: ConnectState
        get() = _currState
    private val _curr = MutableLiveData(_currState)
    override val curr: LiveData<ConnectState> = _curr

    private var lastState = _currState
    private val handlerList = mutableListOf<OnConnectHandler>()

    private val callBack = object : GattCallbackDispatcher(log) {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                val cb = this
                if (gatt == null) {
                    log.log { "gatt null." }
                    _currState = ConnectState.Disconnecting
                    return
                }
                val connector = this@BleConnector
                helper?.launch {
                    var handlerResult = true
                    kotlin.run {
                        handlerList.forEach {
                            handlerResult = handlerResult && it.onConnect(connector, gatt, cb)
                            log.log { "[${dev.mac}] onConnect(${it.javaClass.simpleName}): $handlerResult." }
                            if (!handlerResult) {
                                return@run
                            }
                        }
                    }

                    _currState = if (handlerResult) {
                        ConnectState.Connected
                    } else {
                        ConnectState.Disconnecting
                    }
                }
                return
            }
            _currState = ConnectState.from(newState)
        }
    }

    @SuppressLint("MissingPermission")
    override fun connect(
        timeout: Long,
        connectListener: ConnectListener?
    ): Boolean {
        this.connectListener = connectListener
        helper?.launch {
            if (!dev.find(timeout)) {
                log.log { "[${dev.mac}] not find." }
                connectListener?.onConnectFail(dev.mac, ConnectFail.DevNotScanned)
                return@launch
            }
            val device = dev.dev
            if (device == null) {
                connectListener?.onConnectFail(dev.mac, ConnectFail.Other)
                return@launch
            }
            withContext(Dispatchers.Main) {
                gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log.log { "[${dev.mac}] CONNECT(TRANSPORT_LE, PHY_LE_1M_MASK)." }
                    device.connectGatt(
                        null,
                        false,
                        callBack,
                        BluetoothDevice.TRANSPORT_LE,
                        BluetoothDevice.PHY_LE_1M_MASK,
                        //在子线程回调，使用handler反而会阻塞回调
                        /*helper?.handler*/
                    )
                } else {
                    log.log { "[${dev.mac}] CONNECT()." }
                    device.connectGatt(null, false, callBack)
                }
            }
            callBack.gatt = gatt
            _currState = ConnectState.Connecting
        }
        return true
    }

    override fun addConnectHandler(handler: OnConnectHandler): Connector {
        handlerList.add(handler)
        return this
    }

    override fun removeConnectHandler(handler: OnConnectHandler): Connector {
        handlerList.remove(handler)
        return this
    }

    override fun stop(): Boolean {
        return disconnectBy(true)
    }

    @SuppressLint("MissingPermission")
    private fun disconnectBy(user: Boolean = false): Boolean {
        log.log { "[${dev.mac}] connect stop() call by user($user) and now($_currState)." }
        if (_currState == ConnectState.Disconnecting || _currState == ConnectState.Disconnected) {
            return true
        }
        runBlocking(Dispatchers.Main) {
            log.log { "[${dev.mac}] DISCONNECTED()." }
            gatt?.disconnect()
        }
        return true
    }

    /**
     * 只能在断开连接之后才能关闭gatt，避免状态错误
     */
    @SuppressLint("MissingPermission")
    private fun closeIfDisconnected() {
        if (_currState != ConnectState.Disconnected) {
            throw IllegalStateException("[${dev.mac}] cannot close after disconnect.")
        }
        log.log { "[${dev.mac}] CLOSE()." }
        try {
            gatt?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        gatt = null
    }
}

