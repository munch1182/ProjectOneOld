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

    var timeout = 60 * 1000L

    var maxMTU = 0

    var transport: Int = BluetoothDevice.TRANSPORT_LE

    @RequiresApi(Build.VERSION_CODES.O)
    var phy: Int = BluetoothDevice.PHY_LE_2M_MASK

    /**
     * 实现需要将发现服务作为连接成功的条件
     *
     * @see onServicesHandler
     */
    var needDiscoverServices = true

    /**
     * 当发现服务时，将此处理的返回值作为连接成功的判断条件, 仅[needDiscoverServices]为true时有效
     *
     * @see needDiscoverServices
     */
    var onServicesHandler: OnServicesHandler? = null

    /**
     * 当常用设置处理完成时，将此处理的返回值作为连接成功的判断条件
     */
    var onDisconnectHandler: OnConnectCompleteHandler? = null
}

interface OnServicesHandler {
    /**
     * 对服务的处理回调
     * @return true 则服务处理成功，进行下一步连接后的检查或设置，否则回调连接失败并自动断开连接
     *
     * 因此此方法的处理需要是同步阻塞的
     *
     * 此方法回调时还不能进行协议发送与接收
     *
     * //todo 如果这这个方法中调用了需要回调的方法如何处理
     */
    fun onServicesDiscovered(gatt: BluetoothGatt): Boolean = false
}

interface OnConnectCompleteHandler {
    /**
     * 当[Connector]检查完设置的参数后，会回调此方法进行自定义的处理或者判断
     *
     * 比如：可以发送协议检查设备是否符合要求，或者进行配对处理
     *
     * @return 当此方法返回true时则连接回调会回调成功，否则会回调连接失败并自动断开连接
     *
     * 因此此方法的处理需要是同步阻塞的
     *
     * 此方法回调时，可以进行协议的发送与接收
     */
    fun onHandleConnectComplete(gatt: BluetoothGatt): Boolean = true
}

sealed class ConnectFail(message: String) : Exception(message) {
    /**
     * 指直接被系统回调[android.bluetooth.BluetoothGattCallback.onConnectionStateChange]时status不为[BluetoothGatt.GATT_SUCCESS]的情形
     */
    open class SystemError(private val status: Int) : ConnectFail("SystemError status: $status") {
        override fun toString() = "SystemError: $status"
    }

    /**
     * 指发现[android.bluetooth.BluetoothGatt.discoverServices]方法返回false或者
     * [android.bluetooth.BluetoothGattCallback.onServicesDiscovered]没有回调的情形
     */
    object ServiceDiscoveredFail : ConnectFail("ServiceDiscoveredFail") {
        override fun toString() = "ServiceDiscoveredFail"
    }

    object MtuSetFail : ConnectFail("MtuSetFail") {
        override fun toString() = "MtuSetFail"
    }

    /**
     * 指不允许连接的情形，比如上个连接对象未断开或者不为null
     */
    class DisallowConnected(private val desc: String = "null") :
        ConnectFail("DisallowConnected: $desc") {
        override fun toString() = "DisallowConnected: $desc"
    }

    class Code133Error : SystemError(133)
}

sealed class DisconnectCause {

    class BySystem(private val status: Int) : DisconnectCause()
    object ByUser : DisconnectCause()
    object ByHelper : DisconnectCause()
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