package com.munch.lib.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.munch.lib.helper.ARSHelper
import com.munch.lib.log.Logger

/**
 * Create by munch1182 on 2021/8/17 9:48.
 */
class BluetoothHelper private constructor() {

    companion object {

        val instance by lazy { BluetoothHelper() }

        fun permissionsScan() = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun getBluetoothIntent() = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

        /**
         * 检查[mac]地址是否有效，即是否是合法的格式
         *
         * 注意：并不能检查地址是否存在
         */
        fun checkMac(mac: String?): Boolean {
            return if (mac == null) false else BluetoothAdapter.checkBluetoothAddress(mac)
        }

        internal val logSystem = Logger().apply {
            tag = "bluetooth-system"
            noStack = true
        }
        internal val logHelper = Logger().apply {
            tag = "bluetooth-helper"
            noStack = true
        }
    }

    private lateinit var context: Context
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
    private val classicScanner by lazy { ClassicScanner() }

    /**
     * 用于统一管理回调
     */
    private val notifyHelper = BluetoothNotifyHelper()

    /**
     * 用于统一管理状态
     */
    private val stateHelper = BluetoothStateHelper().apply {
        onChangeListener = notifyHelper.stateChangeCallback
    }

    /**
     * 用于持有当前使用的扫描器对象
     */
    private var scanner: Scanner? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        instance = BluetoothInstance(context)
        handlerThread = HandlerThread("BLUETOOTH_WORK_THREAD")
        handlerThread.start()
        workHandler = Handler(handlerThread.looper)
        state.currentState = if (instance.isEnable) BluetoothState.IDLE else BluetoothState.CLOSE
    }

    val adapter: BluetoothAdapter?
        get() = set.adapter
    val set: BluetoothInstance
        get() = instance
    val connectedDev: BtDevice?
        get() = null
    val scanListeners: ARSHelper<OnScannerListener>
        get() = notifyHelper.scanListeners
    val stateListeners: ARSHelper<OnStateChangeListener>
        get() = notifyHelper.stateListeners
    val state: BluetoothStateHelper
        get() = stateHelper

    @RequiresPermission(
        allOf = [Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startBleScan() {
        scanner?.cancel()
        scanner = bleScanner
        bleScanner.listener = notifyHelper.scanCallback
        scanner?.start()
    }

    fun bleScanBuilder() = BleScanner.Builder().apply {
        bleScanner.builder = this
    }

    fun startClassicScan() {
        scanner?.cancel()
        scanner = classicScanner
        bleScanner.listener = notifyHelper.scanCallback
        scanner?.start()
    }

    fun stopScan() {
        scanner?.stop()
        scanListeners.clear()
        bleScanner.listener = null
    }

    internal fun newState(@BluetoothState state: Int) {
        stateHelper.currentState = state
    }
}