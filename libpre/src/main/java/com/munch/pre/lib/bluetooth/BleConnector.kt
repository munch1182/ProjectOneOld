package com.munch.pre.lib.bluetooth

import android.bluetooth.*
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.munch.pre.lib.helper.format
import java.util.*

/**
 * Create by munch1182 on 2021/4/26 14:50.
 */
class BleConnector constructor(val device: BtDevice) : Manageable {

    @ConnectState
    private var state: Int = ConnectState.STATE_DISCONNECTED
        set(value) {
            synchronized(this) {
                val old = field
                if (field != value) {
                    field = value
                    stateListener?.onStateChange(old, field)
                    logSystem.withEnable { "onStateChange: $old -> $value" }
                }
            }
        }
    private var gatt: BluetoothGatt? = null
    private val opHelper = OpHelper()
    internal var stateListener: BtConnectStateListener? = null
    internal var connectListener: BtConnectListener? = null

    private val connectCallback = object : BtConnectListener {
        override fun onStart(device: BtDevice) {
            state = ConnectState.STATE_CONNECTING
            connectListener?.onStart(device)
            logHelper.withEnable { "onStart connect:${device.mac}" }
        }

        override fun onConnectFail(device: BtDevice, @ConnectFailReason reason: Int) {
            state = ConnectState.STATE_DISCONNECTED
            connectListener?.onConnectFail(device, reason)
            logHelper.withEnable { "connect fail:${device.mac}, reason:${reason}" }
        }

        override fun onConnectSuccess(device: BtDevice, gatt: BluetoothGatt) {
            state = ConnectState.STATE_CONNECTED
            opHelper.setGatt(gatt)
            connectListener?.onConnectSuccess(device, gatt)
            logHelper.withEnable { "connect success:${device.mac}" }
        }
    }
    private val helper = BluetoothHelper.INSTANCE
    private val logHelper = BluetoothHelper.logHelper
    private val logSystem = BluetoothHelper.logSystem

    /**
     * 系统层的外调
     */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            logSystem.withEnable { "onConnectionStateChange: newState: $newState, states:$status" }
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                //连接成功状态需要等待服务发现完成后更新
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else {
                    state = ConnectState.from(newState)
                }
            } else {
                //连接中回调失败则连接失败
                if (state == ConnectState.STATE_CONNECTING) {
                    connectCallback.onConnectFail(device, ConnectFailReason.FAIL_CONNECT_BY_SYSTEM)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            val gattService = gatt.services
            logSystem.withEnable { "onServicesDiscovered: service: ${gattService.size}, states:$status" }
            val config = helper.config
            if (status != BluetoothGatt.GATT_SUCCESS ||
                !config.onDiscoverService(device, gatt, gattService)
            ) {
                connectCallback.onConnectFail(device, ConnectFailReason.FAIL_FIND_SERVICE)
            } else {
                requestMtu(gatt)
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
            logSystem.withEnable { "onDescriptorRead: status:$status" }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                connectCallback.onConnectFail(device, ConnectFailReason.FAIL_READ_DESCRIPTOR)
            } else {
                requestMtu(gatt)
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            logSystem.withEnable { "onDescriptorWrite: status:$status" }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                connectCallback.onConnectFail(device, ConnectFailReason.FAIL_WRITE_DESCRIPTOR)
            } else {
                requestMtu(gatt)
            }
        }

        /**
         * 此流程将[onMtuChanged]作为连接的最后一步，所以每一个回调都会调用此方法
         */
        private fun requestMtu(gatt: BluetoothGatt) {
            if (state == ConnectState.STATE_CONNECTED) {
                return
            }
            val config = helper.config
            if (config.mtu != -1) {
                gatt.requestMtu(config.mtu)
                logSystem.withEnable { "requestMtu: ${config.mtu}" }
            } else {
                this@BleConnector.gatt = gatt
                connectCallback.onConnectSuccess(device, gatt)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            logSystem.withEnable { "onMtuChanged: request: ${helper.config.mtu}, mtu: $mtu, states:$status" }
            if (helper.config.onMtuChanged(gatt, mtu, status)) {
                this@BleConnector.gatt = gatt
                helper.config.mtu = mtu
                connectCallback.onConnectSuccess(device, gatt)
            } else {
                connectCallback.onConnectFail(device, ConnectFailReason.FAIL_REQUEST_MTU)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            opHelper.characteristicListener.onRead(characteristic)
            logSystem.withEnable {
                val bytes = characteristic?.value
                "onCharacteristicChanged: ${System.currentTimeMillis()}, ${bytes?.format() ?: "[]"}"
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            logSystem.withEnable {
                val bytes = characteristic?.value
                "onCharacteristicRead: ${System.currentTimeMillis()}, ${bytes?.format() ?: "[]"}"
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            opHelper.characteristicListener.onSend(characteristic, status)
            logSystem.withEnable {
                val bytes = characteristic?.value
                "onCharacteristicWrite: ${System.currentTimeMillis()}, ${bytes?.format() ?: "[]"}"
            }
        }
    }

    /**
     * @param connectListener 当连接成功或者失败后，该connectListener会被自动移除
     *
     * 如果要自行维护listener的生命周期，使用[BluetoothHelper.connectListeners]
     */
    fun connectCompat(connectListener: BtConnectListener? = null) {
        connectListener?.let {
            helper.tempConnectListeners.add(it)
            helper.connectListeners.add(it)
        }
        connectCallback.onStart(device)
        device.device.connectGatt(null, false, gattCallback)
    }

    /**
     * @param handler 连接的回调会回调到该handler所在线程
     * @param connectListener 当连接成功或者失败后，该connectListener会被自动移除
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun connect(
        transport: Int = BluetoothDevice.TRANSPORT_AUTO,
        phy: Int = BluetoothDevice.PHY_LE_1M_MASK,
        handler: Handler? = helper.handler,
        connectListener: BtConnectListener? = null
    ) {
        connectListener?.let {
            helper.tempConnectListeners.add(it)
            helper.connectListeners.add(it)
        }
        connectCallback.onStart(device)
        device.device.connectGatt(null, false, gattCallback, transport, phy, handler)
    }

    fun disconnect() {
        state = ConnectState.STATE_DISCONNECTING
        gatt?.disconnect()
        gatt = null
    }

    @ConnectState
    fun getState() = state

    fun send(byteArray: ByteArray) {
        send(SendPack(byteArray))
    }

    fun send(pack: SendPack) {
        opHelper.send(pack)
    }

    override fun cancel() {
        logHelper.withEnable { "connector cancel" }
        disconnect()
        opHelper.cancel()
    }

    override fun destroy() {
        logHelper.withEnable { "connector destroy" }
        cancel()
        opHelper.destroy()
    }

    internal fun setWrite(write: BluetoothGattCharacteristic) {
        opHelper.setWrite(write)
    }

    internal fun setNotify(notify: BluetoothGattCharacteristic) {
        opHelper.setNotify(notify)
    }

}