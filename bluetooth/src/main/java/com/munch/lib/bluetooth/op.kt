package com.munch.lib.bluetooth

import android.bluetooth.BluetoothGatt
import androidx.lifecycle.LiveData

/**
 * Created by munch1182 on 2022/5/19 0:03.
 */
interface IBluetoothStop {

    /**
     * 如果当前正在扫描，则停止扫描
     * 如果当前正在连接中，则断开连接中
     *
     * @return 是否成功停止
     */
    fun stop(): Boolean
}

interface Scanner : IBluetoothStop {

    /**
     * 启动蓝牙扫描
     *
     * @param target 用于扫描的目标
     * @param listener 扫描结果的回调，会在关闭扫描时自动取消回调的注册
     * @return 是否启动成功
     */
    fun scan(target: ScanTarget = ScanTarget(), listener: ScanListener? = null): Boolean

    /**
     * 注册一个全局的扫描回调，需要手动解除注册
     */
    fun registerScanListener(t: ScanListener?)
    fun unregisterScanListener(t: ScanListener? = null)

    val isScanning: LiveData<Boolean>
}

interface ScanListener {
    fun onScanned(dev: BluetoothDev, map: LinkedHashMap<String, BluetoothDev>)

    fun onStart() {}
    fun onComplete() {}
}

interface SimpleScanListener : ScanListener {
    override fun onScanned(dev: BluetoothDev, map: LinkedHashMap<String, BluetoothDev>) {
        onScanned(dev)
    }

    fun onScanned(dev: BluetoothDev)
}

sealed class ConnectFail {

    class CodeError(val code: Int) : ConnectFail()

    object DevNotScanned : ConnectFail()

    object Other : ConnectFail()
}

/**
 * 表示单次连接活动的结果
 *
 * 不表示gatt的连接状态
 */
interface ConnectListener {

    fun onStart(mac: String)

    fun onConnectSuccess(mac: String)

    fun onConnectFail(mac: String, fail: ConnectFail)
}

interface Connector : IBluetoothStop {

    fun connect(timeout: Long = 15 * 1000L, connectListener: ConnectListener? = null): Boolean

    fun setConnectHandler(connectHandler: OnConnectHandler? = null)
}

interface OnConnectHandler {

    suspend fun onConnect(
        connector: Connector,
        gatt: BluetoothGatt,
        callbackDispatch: GattCallbackDispatch
    ): Boolean
}