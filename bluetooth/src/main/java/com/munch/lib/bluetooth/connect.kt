package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Build
import com.munch.lib.log.Logger
import kotlinx.coroutines.launch

/**
 * Created by munch1182 on 2022/5/19 1:52.
 */
sealed class ConnectState {
    object Disconnected : ConnectState() {
        override fun toString() = "Disconnected"
    }

    object Connecting : ConnectState() {
        override fun toString() = "Connecting"
    }

    object Connected : ConnectState() {
        override fun toString() = "Connected"
    }

    object Disconnecting : ConnectState() {
        override fun toString() = "Disconnecting"
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

@SuppressLint("MissingPermission")
class BleConnector(private val dev: BluetoothDev, private val log: Logger) : Connector {

    private var connectListener: ConnectListener? = null
    internal var helper: BluetoothHelper? = null
    private var gatt: BluetoothGatt? = null
    private var connectHandler: OnConnectHandler? = null

    private var currState: ConnectState = ConnectState.Disconnected
        get() = synchronized(this) { field }
        set(value) {
            synchronized(this) {
                lastState = field
                field = value
            }
            log.log { "[${dev.mac}] connect state: $lastState->$field." }
            if (lastState != ConnectState.Disconnected
                && field == ConnectState.Disconnected
            ) {
                //dev
                throw IllegalStateException("connector state: $lastState -> $field")
            }
            if (field == ConnectState.Disconnecting) {
                stopBy()
            }
        }
    private var lastState = currState

    private val callBack = object : GattCallbackDispatch(log) {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            /*val state = ConnectState.from(newState)
            currState = state*/
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                if (connectHandler != null) {
                    val cb = this
                    if (gatt == null) {
                        log.log { "gatt null." }
                        currState = ConnectState.Disconnecting
                        return
                    }
                    helper?.launch {
                        if (connectHandler?.onConnect(this@BleConnector, gatt, cb) != false) {
                            currState = ConnectState.Connected
                        }
                    }
                } else {
                    currState = ConnectState.Connected
                }
            } else {
                currState = ConnectState.from(newState)
            }
        }
    }

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
            gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                device.connectGatt(
                    null,
                    false,
                    callBack,
                    BluetoothDevice.TRANSPORT_LE,
                    BluetoothDevice.PHY_LE_1M_MASK,
                    helper?.handler
                )
            } else {
                device.connectGatt(null, false, callBack)
            }
        }
        return true
    }

    override fun stop(): Boolean {
        return stopBy()
    }

    private fun stopBy(user: Boolean = false): Boolean {
        log.log { "[${dev.mac}] connect stop() call by user($user)." }
        //gatt?.disconnect()
        gatt?.close()
        gatt = null
        return true
    }

    override fun setConnectHandler(connectHandler: OnConnectHandler?) {
        this.connectHandler = connectHandler
    }
}

