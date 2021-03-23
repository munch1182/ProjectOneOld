package com.munch.lib.bt

import android.bluetooth.*
import android.os.Handler
import androidx.annotation.IntDef
import androidx.annotation.RequiresPermission
import com.munch.lib.ATTENTION
import com.munch.lib.helper.AddRemoveSetHelper
import com.munch.lib.helper.closeQuietly
import java.util.*

/**
 * Create by munch1182 on 2021/3/4 11:09.
 */
interface BtConnectListener {

    fun onStart(mac: String)

    fun connectSuccess(mac: String)

    fun connectFail(e: Exception)

    /**
     * 扫描结束，成功或者失败都会调用此方法
     */
    fun onFinish() {}
}

open class ConnectFailException(
    @Reason val reason: Int = Reason.REASON_UNKNOWN,
    override val message: String
) :
    RuntimeException(message) {

    @IntDef(
        Reason.REASON_UNKNOWN,
        Reason.OTHER_DEVICE_CONNECTED,
        Reason.BUSYING,
        Reason.CONNECTING,
        Reason.BLE_SERVICE_FAIL
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Reason {
        companion object {
            /**
             * 未知错误
             */
            const val REASON_UNKNOWN = 0

            /**
             * 其它设备已连接
             */
            const val OTHER_DEVICE_CONNECTED = 1

            /**
             * 当前设备正在执行其它操作，不能进行连接
             */
            const val BUSYING = 2

            /**
             * 当前设备已经在连接中
             */
            const val CONNECTING = 3

            /**
             * 设备发现服务失败
             */
            const val BLE_SERVICE_FAIL = 4
        }
    }

    companion object {


        fun otherConnected(mac: String) =
            ConnectFailException(Reason.OTHER_DEVICE_CONNECTED, "device($mac) had connected")

        fun isBusying() =
            ConnectFailException(Reason.BUSYING, "device is busying, wait a moment")

        fun isConnecting() =
            ConnectFailException(Reason.CONNECTING, "device is connecting, cannot connect again")

        fun serviceFail() = serviceFail(7)
        fun discoverMainServiceFail() = serviceFail(0)
        fun discoverWriteCharFail() = serviceFail(1)
        fun discoverNotifyCharFail() = serviceFail(3)
        fun discoverNotifyDescriptorFail() = serviceFail(4)
        fun setNotifyFail() = serviceFail(5)
        fun setNotifyDescriptorFail() = serviceFail(6)
        private fun serviceFail(type: Int): ConnectFailException {
            val typeStr = when (type) {
                0 -> "discover main"
                1 -> "discover write"
                2 -> "set write"
                3 -> "discover notify"
                4 -> "discover notify descriptor"
                5 -> "set notify"
                6 -> "set notify descriptor"
                else -> ""
            }
            return ConnectFailException(Reason.BLE_SERVICE_FAIL, "fail to $typeStr service")
        }

    }
}

class NoMainUUIDException : ConnectFailException(message = "BLE must need service uuid")

internal sealed class BtConnector {

    protected var connectListener: BtConnectListener? = null
    protected var target: BtDevice? = null
    internal var state: @ConnectState Int = ConnectState.STATE_DISCONNECTED
        set(value) {
            val oldState = field
            field = value
            connectStateListener?.onStateChange(oldState, value)
        }
    protected open var connectStateListener: BtConnectStateListener? = null
    protected open val connectCallBack = object : BtConnectListener {
        override fun onStart(mac: String) {
            connectListener?.onStart(mac)
        }

        override fun connectSuccess(mac: String) {
            connectListener?.connectSuccess(mac)
        }

        override fun connectFail(e: Exception) {
            connectListener?.connectFail(e)
        }

        override fun onFinish() {
            super.onFinish()
            connectListener?.onFinish()
        }
    }

    internal class ClassicConnector(private val thread: Handler) : BtConnector() {

        companion object {
            const val BLUETOOTH_UUID = "00001105-0000-1000-8000-00805F9B34FB"
        }

        private var socket: BluetoothSocket? = null

        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        override fun connect() {
            super.connect()
            socket ?: return
            synchronized(this) {
                updateState2Connecting()
                connectCallBack.onStart(target!!.mac)
            }
            thread.post {
                try {
                    socket!!.connect()
                } catch (e: Exception) {
                    synchronized(this) {
                        updateState2Disconnected()
                        connectCallBack.connectFail(e)
                        connectCallBack.onFinish()
                    }
                    return@post
                }
                synchronized(this) {
                    updateState2Connected()
                    connectCallBack.connectSuccess(target!!.mac)
                    connectCallBack.onFinish()
                }
            }

        }

        override fun disconnect() {
            super.disconnect()
            updateState2Disconnected()
            socket?.closeQuietly()
            socket = null
        }

        @RequiresPermission(android.Manifest.permission.BLUETOOTH)
        override fun setTarget(target: BtDevice): BtConnector {
            socket = target.device.createInsecureRfcommSocketToServiceRecord(
                UUID.fromString(BLUETOOTH_UUID)
            )
            return super.setTarget(target)
        }
    }

    internal class BleConnector : BtConnector() {
        private var bleDataHelper: BleDataHelper? = null
        private val gattCallBack = object : BluetoothGattCallback() {
            //此方法目前在断开时没有回调
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                //业务的连接状态成功需要在服务发现和设置完毕后
                if (newState != BluetoothGatt.STATE_CONNECTED) {
                    state = stateChange(newState)
                } else {
                    gatt?.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    connectCallBack.connectFail(ConnectFailException.serviceFail())
                    return
                }
                val btConfig = BluetoothHelper.getInstance().btConfig
                if (btConfig?.UUID_MAIN_SERVER == null) {
                    connectCallBack.connectFail(NoMainUUIDException())
                    return
                }
                val mainService = gatt.getService(UUID.fromString(btConfig.UUID_MAIN_SERVER))
                if (mainService == null) {
                    connectCallBack.connectFail(ConnectFailException.discoverMainServiceFail())
                    return
                }

                if (bleDataHelper != null) {
                    bleDataHelper!!.release()
                } else {
                    bleDataHelper = BleDataHelper()
                }
                bleDataHelper!!.setGatt(gatt)

                val uuidNotify = btConfig.UUID_NOTIFY
                if (uuidNotify != null) {
                    val notifyService = mainService.getCharacteristic(UUID.fromString(uuidNotify))
                    if (notifyService == null) {
                        connectCallBack.connectFail(ConnectFailException.discoverNotifyCharFail())
                        return
                    }
                    if (!gatt.setCharacteristicNotification(notifyService, true)) {
                        connectCallBack.connectFail(ConnectFailException.setNotifyFail())
                        return
                    }
                    val descriptorNotify = btConfig.UUID_DESCRIPTOR_NOTIFY
                    if (descriptorNotify != null) {
                        val notifyDescriptor =
                            notifyService.getDescriptor(UUID.fromString(descriptorNotify))
                        if (notifyDescriptor == null) {
                            connectCallBack.connectFail(ConnectFailException.discoverNotifyDescriptorFail())
                            return
                        }
                        notifyDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        if (!gatt.writeDescriptor(notifyDescriptor)) {
                            connectCallBack.connectFail(ConnectFailException.setNotifyDescriptorFail())
                            return
                        }
                    }
                    bleDataHelper?.setNotify(notifyService)
                }

                val uuidWrite = btConfig.UUID_WRITE
                if (uuidWrite != null) {
                    val writeService = mainService.getCharacteristic(UUID.fromString(uuidWrite))
                    if (writeService == null) {
                        connectCallBack.connectFail(ConnectFailException.discoverWriteCharFail())
                        return
                    }
                    bleDataHelper?.setWrite(writeService)
                }
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    connectCallBack.connectFail(ConnectFailException.setNotifyDescriptorFail())
                    return
                }
                BluetoothHelper.getInstance().btConfig?.MTU_MAX?.run {
                    gatt?.requestMtu(this)
                } ?: kotlin.run {
                    connected()
                }
            }

            private fun connected() {
                connectCallBack.connectSuccess(target!!.mac)
            }

            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)
                BluetoothHelper.getInstance().btConfig?.MTU_MAX?.run {
                    if (mtu != this) {
                        gatt?.requestMtu(this)
                        return
                    }
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        connected()
                        return
                    }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                characteristic ?: return
                bleDataHelper?.onCharacteristicChanged(characteristic)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                bleDataHelper?.onCharacteristicWrite(characteristic, status)
            }

        }

        override val connectCallBack = object : BtConnectListener {
            override fun onStart(mac: String) {
                updateState2Connecting()
                connectListener?.onStart(mac)
            }

            override fun connectSuccess(mac: String) {
                updateState2Connected()
                connectListener?.connectSuccess(mac)
                connectListener?.onFinish()
            }

            override fun connectFail(e: Exception) {
                updateState2Disconnected()
                //断开因服务失败等情形的实际连接
                disconnect()
                bleDataHelper?.release()
                connectListener?.connectFail(e)
                connectListener?.onFinish()
            }
        }
        private var connectGatt: BluetoothGatt? = null

        override fun connect() {
            super.connect()
            if (target != null && unConnected()) {
                connectCallBack.onStart(target!!.mac)
                val context = BluetoothHelper.getInstance().context
                connectGatt = target!!.device.connectGatt(context, false, gattCallBack)
            }
        }

        override fun disconnect() {
            super.disconnect()
            if (connectGatt != null) {
                updateState2Disconnecting()
                connectGatt!!.apply {
                    disconnect()
                    close()
                }
                connectGatt = null
                updateState2Disconnected()
            }
        }

        /**
         * 将系统状态转为[state]
         * 实际上应该例如将[BluetoothProfile.STATE_CONNECTED]转为[ConnectState.STATE_CONNECTED]
         * 但因为设值的时候已经将值设为一致，所以可以省略
         * 如有新增需要手动转换
         */
        @ATTENTION
        fun stateChange(state: Int): @ConnectState Int {
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

    open fun setTarget(target: BtDevice): BtConnector {
        this.target = target
        return this
    }

    fun setConnectStateListener(listener: BtConnectStateListener): BtConnector {
        connectStateListener = listener
        return this
    }

    //<editor-fold desc="state">
    /**
     * 处于未连接状态，才可以进行连接，否则应该先断开或者取消再连接
     */
    fun unConnected() = state == ConnectState.STATE_DISCONNECTED
    fun isConnecting() = state == ConnectState.STATE_CONNECTING
    fun isConnected() = state == ConnectState.STATE_CONNECTED
    fun isDisconnecting() = state == ConnectState.STATE_DISCONNECTING
    fun updateState2Disconnected() {
        state = ConnectState.STATE_DISCONNECTED
    }

    fun updateState2Disconnecting() {
        state = ConnectState.STATE_DISCONNECTING
    }

    fun updateState2Connected() {
        state = ConnectState.STATE_CONNECTED
    }

    fun updateState2Connecting() {
        state = ConnectState.STATE_DISCONNECTING
    }
    //</editor-fold>
}

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@IntDef(
    ConnectState.STATE_CONNECTED,
    ConnectState.STATE_CONNECTING,
    ConnectState.STATE_DISCONNECTED,
    ConnectState.STATE_DISCONNECTING
)
@Retention(AnnotationRetention.SOURCE)
annotation class ConnectState {

    companion object {
        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
        const val STATE_DISCONNECTING = 3
    }
}

/**
 * 设备连接状态回调
 */
interface BtConnectStateListener {

    /**
     * 既然已经有单例，且有分别的判断方法，那么是否可以隐藏ConnectState
     */
    fun onStateChange(@ConnectState oldState: Int, @ConnectState newState: Int)
}

class BtConnectHelper(private val thread: Handler) : AddRemoveSetHelper<BtConnectListener>() {

    private var classicConnector: BtConnector.ClassicConnector? = null
    private var bleConnector: BtConnector.BleConnector? = null
    private fun getConnectListeners() = arrays
    private var currentDevice: BtDevice? = null
    private var onceConnectListener: BtConnectListener? = null
    private var currentConnector: BtConnector? = null
    private val connectCallback = object : BtConnectListener {
        override fun onStart(mac: String) {
            getConnectListeners().forEach { thread.post { it.onStart(mac) } }
        }

        override fun connectSuccess(mac: String) {
            getConnectListeners().forEach { thread.post { it.connectSuccess(mac) } }
        }

        override fun connectFail(e: Exception) {
            getConnectListeners().forEach { thread.post { it.connectFail(e) } }
        }

        override fun onFinish() {
            super.onFinish()
            getConnectListeners().forEach { thread.post { it.onFinish() } }
            clearOnceListener()
        }

        private fun clearOnceListener() {
            if (onceConnectListener != null) {
                getConnectListeners().remove(onceConnectListener)
                onceConnectListener = null
            }
        }
    }
    internal val connectStateListener by lazy {
        return@lazy object : AddRemoveSetHelper<BtConnectStateListener>() {}
    }
    private val connectStateCallback = object : BtConnectStateListener {
        override fun onStateChange(oldState: Int, newState: Int) {
            connectStateListener.arrays.forEach {
                it.onStateChange(oldState, newState)
            }
        }
    }
    internal val connectState: @ConnectState Int
        get() {
            return currentConnector?.state ?: ConnectState.STATE_DISCONNECTED
        }

    private fun getConnector(type: BtType): BtConnector {
        currentConnector = when (type) {
            BtType.Ble -> getBleConnector()
            BtType.Classic -> getClassicConnector()
        }.setConnectListener(connectCallback)
        return currentConnector!!
    }

    private fun getClassicConnector(): BtConnector.ClassicConnector {
        if (classicConnector == null) {
            classicConnector = BtConnector.ClassicConnector(thread)
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
            val connector = getConnector(device.type)
            if (currentDevice != null) {
                when {
                    //当前仍有设备连接
                    //则此处连接失败，需要先断开连接后再调用此方法
                    device != currentDevice -> {
                        connectListener?.connectFail(
                            ConnectFailException.otherConnected(currentDevice!!.mac)
                        )
                        return
                    }
                    //当前设备正在断开连接
                    //则此处连接失败，需要等待当前设备完全断开连接后再调用此方法
                    connector.isDisconnecting() -> {
                        connectListener?.connectFail(ConnectFailException.isBusying())
                        return
                    }
                    //当前设备正在连接
                    //则此处连接失败，因为无法走完BtConnectListener的全部回调
                    connector.isConnecting() -> {
                        connectListener?.connectFail(ConnectFailException.isConnecting())
                        return
                    }
                }
            }
            currentDevice = device
            if (connectListener != null) {
                onceConnectListener = connectListener
                getConnectListeners().add(connectListener)
            }
            //开始连接
            connector.setTarget(currentDevice!!)
                .setConnectStateListener(connectStateCallback)
                .connect()
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

    fun canConnect(): Boolean {
        return connectState == ConnectState.STATE_DISCONNECTED
    }
}