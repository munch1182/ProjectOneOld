package com.munch.lib.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.munch.lib.base.Destroyable
import com.munch.lib.helper.ARSHelper
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/8/17 9:48.
 */
class BluetoothHelper private constructor() : Destroyable {

    companion object {

        val instance by lazy { BluetoothHelper() }

        fun permissionsScan() = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun getBluetoothIntent() = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

        fun openIntent() = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        /**
         * 检查[mac]地址是否有效，即是否是合法的格式
         *
         * 注意：并不能检查地址是否存在
         */
        fun checkMac(mac: String?) =
            mac?.let { BluetoothAdapter.checkBluetoothAddress(it) } ?: false

        internal val logSystem = Logger().apply {
            tag = "bluetooth-system"
            noStack = true
        }
        internal val logHelper = Logger().apply {
            tag = "bluetooth-helper"
            noStack = true
        }
    }

    internal lateinit var context: Context
    private lateinit var instance: BluetoothInstance
    private lateinit var handlerThread: HandlerThread
    internal lateinit var workHandler: Handler

    /**
     * 用于扫描BLE
     */
    private val bleScanner by lazy { BleScanner() }

    /**
     * 用于扫描classic
     */
    private val classicScanner by lazy { ClassicScanner(context) }

    /**
     * 用于统一管理回调
     */
    private val notifyHelper = BluetoothNotifyHelper()

    /**
     * 用于统一管理状态
     */
    private val stateHelper = BluetoothStateHelper()

    /**
     * 用于持有当前使用的扫描器对象
     */
    private var scanner: Scanner? = null
    private var connector: Connector? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) {
            return
        }
        logHelper.withEnable { "BluetoothHelper init" }
        initialized = true
        this.context = context.applicationContext
        instance = BluetoothInstance(context)
        //thread
        handlerThread = HandlerThread("BLUETOOTH_WORK_THREAD")
        handlerThread.start()
        workHandler = Handler(handlerThread.looper)
        //state
        stateHelper.onChangeListener = notifyHelper.stateChangeCallback
        newState(if (instance.isEnable) BluetoothState.IDLE else BluetoothState.CLOSE)
        instance.setStateListener {
            //打开时
            if (it == BluetoothAdapter.STATE_ON) {
                newState(BluetoothState.IDLE)
                //开始关闭蓝牙
            } else if (it == BluetoothAdapter.STATE_TURNING_OFF) {
                stopScan()
                closeConnect()
                newState(BluetoothState.CLOSE)
            }
        }
        instance.setBondStateListener { state, dev ->
            notifyHelper.bluetoothStateCallback.invoke(state, dev)
        }
        instance.setConnectStateListener { state, dev ->
            notifyHelper.bluetoothStateCallback.invoke(state, dev)
        }
    }

    val isInitialized: Boolean
        get() = initialized
    val adapter: BluetoothAdapter?
        get() = set.adapter
    val set: BluetoothInstance
        get() = instance

    /**
     * 已连接的设备，仅已连接状态下有值
     */
    val connectedDev: BluetoothDev?
        get() = state.isConnected.let { if (it) connector?.device else null }

    /**
     * 当前操作的设备，包括连接中和已连接两种状态，包括连接+断开连接两个动作
     */
    val operationDev: BluetoothDev?
        get() = (state.isConnecting || state.isConnected).let { if (it) connector?.device else null }
    val scanListeners: ARSHelper<OnScannerListener>
        get() = notifyHelper.scanListeners
    val stateListeners: ARSHelper<OnStateChangeListener>
        get() = notifyHelper.stateListeners
    val connectListeners: ARSHelper<OnConnectListener>
        get() = notifyHelper.connectListeners

    /**
     * 蓝牙状态回调，用于监听绑定、连接状态
     *
     * @see BluetoothInstanceReceiver
     */
    val bluetoothStateListeners: ARSHelper<(state: Int, dev: BluetoothDevice?) -> Unit>
        get() = notifyHelper.bluetoothStateListeners
    val state: BluetoothStateHelper
        get() = stateHelper

    @RequiresPermission(
        allOf = [Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startBleScan(scanBuilder: ScannerBuilder? = null) {
        startScan(BluetoothType.Ble, scanBuilder)
    }

    fun scanBuilder(type: BluetoothType) = ScannerBuilder(type)

    /**
     * 开始扫描，不同的类型需要不同的权限
     *
     * @see startBleScan
     * @see startClassicScan
     */
    fun startScan(type: BluetoothType, scanBuilder: ScannerBuilder? = null) {
        if (state.isClose || state.isScanning) {
            return
        }
        scanner?.cancel()
        if (type.isBle) {
            scanner = bleScanner
            bleScanner.listener = notifyHelper.scanCallback
            bleScanner.builder = scanBuilder
        } else if (type.isClassic) {
            scanner = classicScanner
            classicScanner.listener = notifyHelper.scanCallback
            classicScanner.builder = scanBuilder
        }
        scanner?.start()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    fun startClassicScan(scanBuilder: ScannerBuilder? = null) {
        startScan(BluetoothType.Classic, scanBuilder)
    }

    fun stopScan() {
        if (state.isClose || !state.isScanning) {
            return
        }
        scanner?.stop()
    }

    /**
     * 连接到该设备
     *
     * 如果当前已有设备连接且连接的不是此设备，则返回false
     * 否则返回true
     *
     * 如果[device]当前已连接，再次调用此方法不会再触发连接回调
     *
     * @return 是否开始连接
     *
     * @see state
     * @see BluetoothStateHelper.isConnected
     * @see connectListeners
     * @see stateListeners
     */
    fun connect(device: BluetoothDev): Boolean {
        return when {
            state.isClose -> false
            device.isConnectedByHelper -> true
            //如果已有连接但连接的不是此设备
            state.isConnected -> false
            device.isBle -> connectBle(device)
            else -> {
                //未实现
                false
            }
        }
    }

    /**
     * 连接到ble设备
     *
     * @param ifCan 是否使用更高的连接参数
     */
    fun connectBle(device: BluetoothDev, ifCan: Boolean = false): Boolean {
        if (!device.isBle) {
            return false
        }
        if (connector == null || connector !is BleConnector || connector!!.device != device) {
            connector = BleConnector(device).apply {
                connectListener = notifyHelper.connectCallback
                connectByAllIfCan = ifCan
            }
        } else {
            (connector as BleConnector).apply {
                connectListener = notifyHelper.connectCallback
                connectByAllIfCan = ifCan
            }
        }
        connector?.connect()
        return true
    }

    /**
     * 断开当前连接的设备，之后仍然可以使用[connect]来重新连接
     *
     * @see closeConnect
     * @see stateListeners
     *
     */
    fun disconnect() {
        if (connector == null || state.isClose || (!state.isConnected && !state.isConnecting)) {
            return
        }
        connector?.disconnect()
    }

    /**
     * 关闭该设备的连接，关闭后不会再收到系统回调
     *
     *  @param removeBond 关闭时是否移除系统绑定
     */
    @SuppressLint("MissingPermission")
    fun closeConnect(removeBond: Boolean = false) {
        if (connector == null || state.isClose || !state.isConnected) {
            return
        }
        if (removeBond) {
            connectedDev?.removeBond()
        }
        connector?.destroy()
        connector = null
    }

    internal fun newState(@BluetoothState state: Int) {
        stateHelper.currentStateVal = state
    }

    override fun destroy() {
        initialized = false
        notifyHelper.destroy()
        handlerThread.quit()
        stopScan()
        closeConnect()
        instance.destroy()
        logHelper.withEnable { "BluetoothHelper destroy" }
    }
}