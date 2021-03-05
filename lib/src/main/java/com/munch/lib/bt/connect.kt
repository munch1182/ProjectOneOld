package com.munch.lib.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.os.Handler
import androidx.annotation.IntDef
import com.munch.lib.ATTENTION
import com.munch.lib.helper.AddRemoveSetHelper
import java.util.*

/**
 * Create by munch1182 on 2021/3/4 11:09.
 */
interface BtConnectListener {

    fun onStart(mac: String)

    fun connectSuccess(mac: String)

    fun connectFail(e: Exception)
}

class ConnectFailException(val reason: Int, override val message: String?) : Exception(message) {

    companion object {

        /**
         * 其它设备已连接
         */
        private const val OTHER_DEVICE_CONNECTED = 0
        private const val DISCOVER_SERVICE_FAIL = 1

        fun otherConnected(mac: String) =
            ConnectFailException(OTHER_DEVICE_CONNECTED, "device($mac) had connected")

        fun discoverServiceFail() = discoverServiceFail(4)
        fun discoverMainServiceFail() = discoverServiceFail(0)
        fun discoverWriteCharFail() = discoverServiceFail(1)
        fun discoverNotifyCharFail() = discoverServiceFail(2)
        fun discoverNotifyDescriptorFail() = discoverServiceFail(3)
        private fun discoverServiceFail(type: Int): ConnectFailException {
            val typeStr = when (type) {
                0 -> "main"
                1 -> "write"
                2 -> "notify"
                3 -> "notify descriptor"
                else -> ""
            }
            return ConnectFailException(DISCOVER_SERVICE_FAIL, "fail to discover $typeStr service")
        }
    }
}

sealed class BtConnector {

    protected var connectListener: BtConnectListener? = null
    protected var target: BtDevice? = null
    protected var state: @State Int = STATE_DISCONNECTED
    protected val connectCallBack = object : BtConnectListener {
        override fun onStart(mac: String) {
            connectListener?.onStart(mac)
        }

        override fun connectSuccess(mac: String) {
            connectListener?.connectSuccess(mac)
        }

        override fun connectFail(e: Exception) {
            connectListener?.connectFail(e)
        }
    }

    class ClassicConnector : BtConnector() {}
    class BleConnector : BtConnector() {
        private val callBack = object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                state = stateChange(newState)
                when (newState) {
                    BluetoothProfile.STATE_DISCONNECTING -> disconnect()
                    BluetoothProfile.STATE_CONNECTING -> {
                    }
                    BluetoothProfile.STATE_CONNECTED -> {
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    connectCallBack.connectFail(ConnectFailException.discoverServiceFail())
                    return
                }
                val btConfig = BluetoothHelper.getInstance().btConfig
                checkUUID(btConfig)
                val mainService = gatt.getService(UUID.fromString(btConfig!!.UUID_MAIN_SERVER))
                if (mainService == null) {
                    connectCallBack.connectFail(ConnectFailException.discoverMainServiceFail())
                    return
                }
                val writeService =
                    mainService.getCharacteristic(UUID.fromString(btConfig.UUID_WRITE))
                if (writeService == null) {
                    connectCallBack.connectFail(ConnectFailException.discoverWriteCharFail())
                    return
                }
                val notifyService =
                    mainService.getCharacteristic(UUID.fromString(btConfig.UUID_NOTIFY))
                if (notifyService == null) {
                    connectCallBack.connectFail(ConnectFailException.discoverNotifyCharFail())
                    return
                }

                val notifyDescriptor =
                    notifyService.getDescriptor(UUID.fromString(btConfig.UUID_DESCRIPTOR_NOTIFY))
                if (notifyDescriptor == null) {
                    connectCallBack.connectFail(ConnectFailException.discoverNotifyDescriptorFail())
                    return
                }
            }


            private fun checkUUID(config: BtConfig?) {
                if (config?.UUID_MAIN_SERVER == null || config.UUID_WRITE == null
                    || config.UUID_NOTIFY == null || config.UUID_DESCRIPTOR_NOTIFY == null
                ) {
                    throw IllegalStateException("ble connect must need service uuid")
                }
            }

        }

        private var connectGatt: BluetoothGatt? = null

        override fun connect() {
            super.connect()
            if (target != null && unConnected()) {
                val context = BluetoothHelper.getInstance().context
                connectGatt = target!!.device.connectGatt(context, false, callBack)
            }
        }

        override fun disconnect() {
            super.disconnect()
            if (connectGatt != null) {
                connectGatt!!.apply {
                    discoverServices()
                    disconnect()
                    close()
                }
                connectGatt = null
            }
        }

        /**
         * 将系统状态转为[state]
         * 实际上应该例如将[BluetoothProfile.STATE_CONNECTED]转为[STATE_CONNECTED]
         * 但因为设值的时候已经将值设为一致，所以可以省略
         * 如有新增需要手动转换
         */
        @ATTENTION
        fun stateChange(state: Int): @State Int {
            return state
        }
    }

    fun setConnectListener(listener: BtConnectListener?): BtConnector {
        this.connectListener = listener
        return this
    }

    open fun connect() {}

    /**
     * 断开连接，或者取消正在连接
     */
    open fun disconnect() {
    }

    open fun write(byteArray: ByteArray) {}

    open fun read(): ByteArray {
        return byteArrayOf()
    }

    fun setTarget(target: BtDevice): BtConnector {
        this.target = target
        return this
    }

    /**
     * 处于未连接状态，才可以进行连接，否则应该先断开或者取消再连接
     */
    fun unConnected() = state == STATE_DISCONNECTED
    fun isConnecting() = state == STATE_CONNECTING
    fun isConnected() = state == STATE_CONNECTED
    fun isDisconnecting() = state == STATE_DISCONNECTING

    @Target(AnnotationTarget.TYPE)
    @IntDef(STATE_CONNECTED, STATE_CONNECTING, STATE_DISCONNECTED, STATE_DISCONNECTING)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

    companion object {

        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
        const val STATE_DISCONNECTING = 3
    }

}

class BtConnectHelper(private val thread: Handler) : AddRemoveSetHelper<BtConnectListener>() {

    private var classicConnector: BtConnector.ClassicConnector? = null
    private var bleConnector: BtConnector.BleConnector? = null
    private var connector: BtConnector? = null
    private fun getConnectListeners() = arrays
    private var currentDevice: BtDevice? = null
    private var onceConnectListener: BtConnectListener? = null
    private val connectCallback = object : BtConnectListener {
        override fun onStart(mac: String) {
            getConnectListeners().forEach { thread.post { it.onStart(mac) } }
        }

        override fun connectSuccess(mac: String) {
            getConnectListeners().forEach { thread.post { it.connectSuccess(mac) } }
            onEnd()
        }

        override fun connectFail(e: Exception) {
            getConnectListeners().forEach { thread.post { it.connectFail(e) } }
            onEnd()
        }

        /**
         * 扫描结果，成功或者失败都会调用此方法
         */
        private fun onEnd() {
            clearOnceListener()
        }

        private fun clearOnceListener() {
            if (onceConnectListener != null) {
                getConnectListeners().remove(onceConnectListener)
                onceConnectListener = null
            }
        }
    }

    private fun getConnector(type: BtType): BtConnector {
        return when (type) {
            BtType.Ble -> getBleConnector()
            BtType.Classic -> getClassicConnector()
        }.setConnectListener(connectCallback)
    }

    private fun getClassicConnector(): BtConnector.ClassicConnector {
        if (classicConnector == null) {
            classicConnector = BtConnector.ClassicConnector()
        }
        return classicConnector!!
    }

    private fun getBleConnector(): BtConnector.BleConnector {
        if (bleConnector == null) {
            bleConnector = BtConnector.BleConnector()
        }
        return bleConnector!!
    }

    /**
     * 如果当前仍有设备连接，则直接连接失败，交由调用者处理逻辑
     * 如果传入设备已经在连接中或者已连接，则视为连接成功
     * 否则连接设备
     *
     * @param device 要连接的设备
     * @param connectListener 该次连接的回调，注意：在连接结束(成功或者失败)后会自动移除该回调
     * 要使用全局回调，使用[add]
     *
     * @see add
     * @see remove
     */
    fun connect(device: BtDevice, connectListener: BtConnectListener?) {
        synchronized(this) {
            val connector = getConnector(currentDevice!!.type)
            if (currentDevice != null) {
                if (device != currentDevice) {
                    connectListener?.connectFail(ConnectFailException.otherConnected(currentDevice!!.mac))
                } else if (!connector.unConnected()) {
                    return
                }
            }
            currentDevice = device
            if (connectListener != null) {
                onceConnectListener = connectListener
                getConnectListeners().add(connectListener)
            }
            connector.setTarget(currentDevice!!).connect()
        }
    }

    fun disConnect() {
        synchronized(this) {
            if (currentDevice != null) {
                getConnector(currentDevice!!.type).disconnect()
                currentDevice = null
            }
        }
    }
}