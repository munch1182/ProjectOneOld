package com.munch.lib.bluetooth.connect

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev

/**
 * Create by munch1182 on 2021/12/7 09:14.
 */
interface IConnect {

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun connect()

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect()
}

/**
 * 只处理连接结果的回调，不包括断开的回调，不要在此处监听蓝牙状态
 */
interface OnConnectListener {

    fun onConnectStart(dev: BluetoothDev) {}

    fun onConnected(dev: BluetoothDev)

    fun onConnectFail(dev: BluetoothDev, fail: ConnectFail) {}
}

class BleConnectSet {

    var timeout = 30 * 1000L

    var transport: Int = BluetoothDevice.TRANSPORT_LE

    @RequiresApi(Build.VERSION_CODES.O)
    var phy: Int = BluetoothDevice.PHY_LE_2M_MASK

    /**
     * 系统回调后，将此处理的返回值作为连接成功的判断条件
     */
    var onConnectSet: OnConnectSet? = null

    var onConnectComplete: OnConnectComplete? = null
}

interface OnConnectSet {
    /**
     * 当系统回调连接成功后，会回调此方法，可以在此方法中进行自定义的处理，比如发现服务或其相关的设置
     * 注意：此方法的处理需要是同步阻塞的
     *
     * @return 当此方法返回null时则进行下一阶段的回调判断，否则会以该失败原因回调连接失败并自动断开连接
     *
     * 此方法不能进行蓝牙数据的发送，因此此时还未设置蓝牙数据的相关处理
     *
     * @see GattWrapper 此类中的方法的返回是同步的
     */
    fun onConnectSet(gatt: GattWrapper): ConnectFail? = null
}

interface OnConnectComplete {

    /**
     * 当连接设置完成并且成功后，会回调此方法来进行最后的连接判断，此方法是最后一次判断
     * 注意：此方法的处理需要是同步阻塞的
     *
     * @return 返回true则会直接回调连接成功，否则会回调连接失败
     *
     * 此方法可以进行蓝牙数据的发送
     */
    fun onConnectComplete(dev: BluetoothDev): Boolean = true
}

sealed class ConnectFail(message: String) : Exception(message) {
    /**
     * 指直接被系统回调[android.bluetooth.BluetoothGattCallback.onConnectionStateChange]时status不为[BluetoothGatt.GATT_SUCCESS]的情形
     */
    open class SystemError(val status: Int) : ConnectFail("SystemError status: $status")

    /**
     * 指发现[android.bluetooth.BluetoothGatt.discoverServices]方法返回false或者
     * [android.bluetooth.BluetoothGattCallback.onServicesDiscovered]没有回调的情形
     */
    class ServiceDiscoveredFail(desc: String? = null) : ConnectFail("ServiceDiscoveredFail: $desc")

    object MtuSetFail : ConnectFail("MtuSetFail")

    /**
     * 指不允许连接的情形，比如上个连接对象未断开或者不为null
     */
    class DisallowConnect(desc: String = "null") : ConnectFail("DisallowConnected: $desc")

    object WriteDescriptorFail : ConnectFail("WriteDescriptorFail")

    object NotificationSetFail : ConnectFail("SetNotificationFail")

    class Code133Error : SystemError(133)

    class Timeout(timeout: Long) : ConnectFail("Timeout($timeout ms)")

    class Error(message: String) : ConnectFail(message)

    override fun toString() = message ?: "ConnectFail"
}

sealed class DisconnectCause {

    class BySystem(private val status: Int) : DisconnectCause() {
        override fun toString() = "Disconnect by System: $status"
    }

    object ByUser : DisconnectCause() {
        override fun toString() = "Disconnect by User"
    }

    object ByHelper : DisconnectCause() {
        override fun toString() = "Disconnect by Helper"
    }
}

sealed class ConnectState {

    object CONNECTING : ConnectState() {
        override fun toString() = "CONNECTING"
    }

    object CONNECTED : ConnectState() {
        override fun toString() = "CONNECTED"
    }


    object DISCONNECTING : ConnectState() {
        override fun toString() = "DISCONNECTING"
    }

    object DISCONNECTED : ConnectState() {
        override fun toString() = "DISCONNECTED"
    }

    val isDisconnected
        get() = this is DISCONNECTED
    val isDisconnecting
        get() = this is DISCONNECTING
    val isConnecting
        get() = this is CONNECTING
    val isConnected
        get() = this is CONNECTED
    val canConnect
        get() = isDisconnected
}