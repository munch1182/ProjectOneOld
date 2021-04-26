package com.munch.pre.lib.bluetooth

import android.bluetooth.*
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.base.Destroyable
import com.munch.pre.lib.helper.ARSHelper
import java.io.Closeable

/**
 * Create by munch1182 on 2021/4/26 14:50.
 */
class BleDevice constructor(private val device: BtDevice) : Cancelable, Destroyable {

    init {
        BluetoothHelper.INSTANCE.setCurrent(this)
    }

    @ConnectState
    private var state: Int = ConnectState.STATE_DISCONNECTED
        set(value) {
            if (field != value) {
                val old = field
                field = value
                stateListener?.onStateChange(value)
                stateListener?.onStateChange(old, field)
            }
        }
    private var gatt: BluetoothGatt? = null
    private val opHelper = OpHelper()
    var stateListener: BtConnectStateListener? = null
    var connectListener: BtConnectListener? = null
    private val connectCallback = object : BtConnectListener {
        override fun onStart(device: BtDevice) {
            connectListener?.onStart(device)
        }

        override fun onConnecting(device: BtDevice) {
            connectListener?.onConnecting(device)
        }

        override fun onConnectFail(@ConnectFailReason reason: Int) {
            connectListener?.onConnectFail(reason)
        }

        override fun onConnectSuccess(gatt: BluetoothGatt) {
            connectListener?.onConnectSuccess(gatt)
        }

        override fun onDiscoverService(gattService: MutableList<BluetoothGattService>): Boolean {
            return connectListener?.onDiscoverService(gattService)
                ?: super.onDiscoverService(gattService)
        }
    }

    /**
     * 系统层的外调
     */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (gatt == null || status != BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED || newState == BluetoothProfile.STATE_CONNECTED) {
                    connectCallback.onConnectFail(ConnectFailReason.FILE_CONNECT_BY_SYSTEM)
                }
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status != BluetoothGatt.GATT_SUCCESS || !connectCallback.onDiscoverService(gatt.services)) {
                connectCallback.onConnectFail(ConnectFailReason.FILE_FIND_SERVICE)
            } else {
                this@BleDevice.gatt = gatt
                connectCallback.onConnectSuccess(gatt)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            opHelper.characteristicListener.onSend(characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            opHelper.characteristicListener.onRead(characteristic, status)
        }
    }

    fun connect() {
        device.device.connectGatt(null, false, gattCallback)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connect(
        transport: Int = BluetoothDevice.TRANSPORT_AUTO,
        phy: Int = BluetoothDevice.PHY_LE_1M_MASK,
        handler: Handler? = null
    ) {
        connectCallback.onConnecting(device)
        device.device.connectGatt(null, false, gattCallback, transport, phy, handler)
    }


    fun disconnect() {
        gatt?.disconnect()
        gatt = null
    }

    override fun cancel() {
        opHelper.cancel()
    }

    override fun destroy() {
        opHelper.destroy()
    }

    internal class OpHelper : Cancelable, Destroyable {

        val characteristicListener = object : CharacteristicChangedListener {
            override fun onRead(characteristic: BluetoothGattCharacteristic?, status: Int) {
            }

            override fun onSend(characteristic: BluetoothGattCharacteristic?) {
            }
        }
        private var gatt: BluetoothGatt? = null

        fun setGatt(gatt: BluetoothGatt) {
            this.gatt = gatt
        }

        override fun cancel() {
            gatt = null
        }

        override fun destroy() {
            cancel()
        }
    }

    internal interface CharacteristicChangedListener {

        fun onRead(characteristic: BluetoothGattCharacteristic?, status: Int)

        fun onSend(characteristic: BluetoothGattCharacteristic?)
    }

}