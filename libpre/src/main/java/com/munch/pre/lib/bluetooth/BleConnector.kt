package com.munch.pre.lib.bluetooth

import android.bluetooth.*
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.base.Destroyable
import java.util.*

/**
 * Create by munch1182 on 2021/4/26 14:50.
 */
class BleConnector constructor(val device: BtDevice) : Cancelable, Destroyable {

    @ConnectState
    private var state: Int = ConnectState.STATE_DISCONNECTED
        set(value) {
            synchronized(this) {
                val old = field
                if (field != value) {
                    field = value
                    stateListener?.onStateChange(old, field)
                    BluetoothHelper.logSystem.withEnable { "onStateChange: $old -> $value" }
                }
            }
        }
    private var gatt: BluetoothGatt? = null
    private val opHelper = OpHelper()
    internal var stateListener: BtConnectStateListener? = null
    internal var connectListener: BtConnectListener? = null
    private val connectCallback = object : BtConnectListener {
        override fun onStart(device: BtDevice) {
            BluetoothHelper.logHelper.withEnable { "onStart connect:${device.mac}" }
            state = ConnectState.STATE_CONNECTING
            connectListener?.onStart(device)
        }

        override fun onConnectFail(device: BtDevice, @ConnectFailReason reason: Int) {
            state = ConnectState.STATE_DISCONNECTED
            BluetoothHelper.logHelper.withEnable { "connect fail:${device.mac}, reason:${reason}" }
            connectListener?.onConnectFail(device, reason)
        }

        override fun onConnectSuccess(device: BtDevice, gatt: BluetoothGatt) {
            state = ConnectState.STATE_CONNECTED
            opHelper.setGatt(gatt)
            BluetoothHelper.logHelper.withEnable { "connect success:${device.mac}" }
            connectListener?.onConnectSuccess(device, gatt)
        }
    }

    /**
     * 系统层的外调
     */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            BluetoothHelper.logSystem.withEnable { "onConnectionStateChange: newState: $newState, states:$status" }
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
            BluetoothHelper.logSystem.withEnable { "onServicesDiscovered: service: ${gattService.size}, states:$status" }
            val config = BluetoothHelper.INSTANCE.config
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
            BluetoothHelper.logSystem.withEnable { "onDescriptorRead: status:$status" }
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
            BluetoothHelper.logSystem.withEnable { "onDescriptorWrite: status:$status" }
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
            val config = BluetoothHelper.INSTANCE.config
            if (config.mtu != -1) {
                gatt.requestMtu(config.mtu)
                BluetoothHelper.logSystem.withEnable { "requestMtu: ${config.mtu}" }
            } else {
                this@BleConnector.gatt = gatt
                connectCallback.onConnectSuccess(device, gatt)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            val request = BluetoothHelper.INSTANCE.config.mtu
            BluetoothHelper.logSystem.withEnable { "onMtuChanged: request: $request, mtu: $mtu, states:$status" }
            if (BluetoothHelper.INSTANCE.config.onMtuChanged(gatt, mtu, status)) {
                this@BleConnector.gatt = gatt
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
            BluetoothHelper.logSystem.withEnable {
                val bytes = characteristic?.value
                val byteStr = if (bytes == null) "[]" else BytesHelper.format(bytes)
                "onCharacteristicChanged: $byteStr"
            }
            opHelper.characteristicListener.onRead(characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            BluetoothHelper.logSystem.withEnable {
                val bytes = characteristic?.value
                val byteStr = if (bytes == null) "[]" else BytesHelper.format(bytes)
                "onCharacteristicRead: $byteStr"
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            BluetoothHelper.logSystem.withEnable {
                "onCharacteristicChanged: ${System.currentTimeMillis()}"
            }
            opHelper.characteristicListener.onSend(characteristic, status)
        }
    }

    /**
     * @param connectListener 当连接成功或者失败后，该connectListener会被自动移除
     *
     * 如果要自行维护listener的生命周期，使用[BluetoothHelper.connectListeners]
     */
    fun connectCompat(connectListener: BtConnectListener? = null) {
        if (connectListener != null) {
            BluetoothHelper.INSTANCE.tempConnectListener.add(connectListener)
            BluetoothHelper.INSTANCE.connectListeners.add(connectListener)
        }
        connectCallback.onStart(device)
        device.device.connectGatt(null, false, gattCallback)
    }

    /**
     * @param handler 连接的回调会回调到该handler所在线程
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun connect(
        transport: Int = BluetoothDevice.TRANSPORT_AUTO,
        phy: Int = BluetoothDevice.PHY_LE_1M_MASK,
        handler: Handler? = BluetoothHelper.INSTANCE.handler,
        connectListener: BtConnectListener? = null
    ) {
        if (connectListener != null) {
            BluetoothHelper.INSTANCE.tempConnectListener.add(connectListener)
            BluetoothHelper.INSTANCE.connectListeners.add(connectListener)
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

    override fun cancel() {
        BluetoothHelper.logHelper.withEnable { "connector cancel" }
        disconnect()
        opHelper.cancel()
    }

    override fun destroy() {
        BluetoothHelper.logHelper.withEnable { "connector destroy" }
        cancel()
        opHelper.destroy()
    }

    fun setWrite(write: BluetoothGattCharacteristic) {
        opHelper.setWrite(write)
    }

    fun setNotify(notify: BluetoothGattCharacteristic) {
        opHelper.setNotify(notify)
    }

    fun send(byteArray: ByteArray) {
        opHelper.send(byteArray)
    }

    internal class OpHelper : Cancelable, Destroyable {

        private val logHelp = BluetoothHelper.logHelper
        val characteristicListener = object : CharacteristicChangedListener {
            override fun onRead(characteristic: BluetoothGattCharacteristic?) {
                logHelp.withEnable { "${System.currentTimeMillis()}: onRead" }
            }

            override fun onSend(characteristic: BluetoothGattCharacteristic?, status: Int) {
                logHelp.withEnable { "${System.currentTimeMillis()}: onSend: status: $status" }
            }
        }
        private var gatt: BluetoothGatt? = null
        private var write: BluetoothGattCharacteristic? = null
        private var notify: BluetoothGattCharacteristic? = null
        private val bytesList = LinkedList<ByteArray>()

        fun send(bytes: ByteArray) {
            //添加到队列等待当前执行完毕
            /*bytesList.add(bytes)*/
            BluetoothHelper.INSTANCE.handler.post { writeByte(bytes) }
            BluetoothHelper.logHelper.withEnable {
                //此方法会延迟速度
                val isNull = if (gatt == null || write == null) "gatt/write: null, " else ""
                "${isNull}${System.currentTimeMillis()}: send: ${BytesHelper.format(bytes)}"
            }
        }

        private fun writeByte(bytes: ByteArray) {
            synchronized(this) {
                if (gatt != null) {
                    write?.value = bytes
                    gatt?.writeCharacteristic(write)
                }
            }
        }

        fun setGatt(gatt: BluetoothGatt) {
            this.gatt = gatt
            BluetoothHelper.logHelper.withEnable { "setGatt" }
        }

        override fun cancel() {
            synchronized(this) {
                gatt?.close()
                gatt = null
                write = null
                notify = null
                bytesList.clear()
            }
        }

        override fun destroy() {
            cancel()
        }

        fun setWrite(write: BluetoothGattCharacteristic) {
            this.write = write
            BluetoothHelper.logHelper.withEnable { "setWrite" }
        }

        fun setNotify(notify: BluetoothGattCharacteristic) {
            this.notify = notify
            BluetoothHelper.logHelper.withEnable { "setNotify" }
        }
    }

    internal interface CharacteristicChangedListener {

        fun onRead(characteristic: BluetoothGattCharacteristic?)

        fun onSend(characteristic: BluetoothGattCharacteristic?, status: Int)
    }

}