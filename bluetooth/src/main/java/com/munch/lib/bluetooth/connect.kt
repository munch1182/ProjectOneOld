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

    private var currState: ConnectState = ConnectState.Disconnected

    private val callBack = GattCallbackDispatch(log)

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
            /*callBack.onStateChange.add { _, _ ->

            }*/
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
}

