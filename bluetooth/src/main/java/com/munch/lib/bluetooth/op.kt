package com.munch.lib.bluetooth

import android.bluetooth.BluetoothGatt
import androidx.lifecycle.LiveData
import com.munch.lib.extend.HandlerDispatcher

/**
 * Created by munch1182 on 2022/5/19 0:03.
 */
internal class BluetoothDispatcher : HandlerDispatcher("bluetooth_thread") {

    override fun toString(): String = "BluetoothDispatcher"
}

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

    val isScanningNow: Boolean
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

interface Connector : IBluetoothStop, IConnectHandler {

    /**
     * 连接设备
     *
     * 只能在非连接状态下才能连接，调用时应该使用队列，而不能并发
     */
    fun connect(timeout: Long = 15 * 1000L, connectListener: ConnectListener? = null): Boolean

    override fun addConnectHandler(handler: OnConnectHandler): Connector
    override fun removeConnectHandler(handler: OnConnectHandler): Connector

    val curr: LiveData<ConnectState>

    val currState: ConnectState
}

interface IConnectHandler {
    /**
     * 添加一个蓝牙连接的处理器
     *
     * @see OnConnectHandler
     */
    fun addConnectHandler(handler: OnConnectHandler): IConnectHandler
    fun removeConnectHandler(handler: OnConnectHandler): IConnectHandler
}

interface OnConnectHandler {

    /**
     * 当蓝牙连接成功后，会回调此方法，可以对gatt进行自定义处理并返回结果
     * 如果自定义结果返回false，则会视为连接失败，进行断开操作并回调状态为连接失败
     * 否则，则会循环回调每一个处理方法，如果全都成功，则视为连接成功
     */
    suspend fun onConnect(
        connector: Connector,
        gatt: BluetoothGatt,
        dispatcher: GattCallbackDispatcher
    ): Boolean
}