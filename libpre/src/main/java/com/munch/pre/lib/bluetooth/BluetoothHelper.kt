package com.munch.pre.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.util.ArrayMap
import androidx.annotation.RequiresPermission
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.helper.ARSHelper
import com.munch.pre.lib.log.Logger

/**
 * Create by munch1182 on 2021/4/8 10:55.
 */
class BluetoothHelper private constructor() {

    companion object {
        val INSTANCE by lazy { BluetoothHelper() }

        fun permissions() = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun getBtSetIntent() = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

        fun openIntent() = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        fun checkMac(mac: String) = BluetoothAdapter.checkBluetoothAddress(mac)

        private const val HT_NAME = "BLUETOOTH_HANDLER_THREAD"

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
    internal lateinit var device: BtInstance
    internal val btAdapter: BluetoothAdapter? by lazy { device.btAdapter }
    private val cancelable = mutableListOf<Cancelable>()
    private lateinit var handlerThread: HandlerThread
    internal lateinit var handler: Handler

    private var bleConnector = ArrayMap<String, BleConnector>()
    private val bleScanner = BleScanner()
    private var currentConnector: BleConnector? = null

    val scanListeners = object : ARSHelper<BtScanListener>() {}
    private val tempScanListeners = mutableListOf<BtScanListener>()
    private val scanCallback = object : BtScanListener {
        override fun onStart() {
            logHelper.withEnable {
                val filter = bleScanner.getFilter()
                    ?.joinToString(
                        prefix = "[",
                        postfix = "]"
                    ) { "name=${it.deviceName},mac=${it.deviceAddress}" }
                "scan onStart: filter: ${filter ?: "null"} "
            }
            notify(scanListeners) { it.onStart() }
        }

        override fun onScan(device: BtDevice) {
            logHelper.withEnable { "scan onScan: device: ${device.mac}" }
            notify(scanListeners) { it.onScan(device) }
        }

        override fun onEnd(devices: MutableList<BtDevice>) {
            logHelper.withEnable { "scan onEnd: devices: ${devices.size}" }
            notify(scanListeners) { it.onEnd(devices) }
            onFinish()
        }

        private fun onFinish() {
            handler.post { tempScanListeners.forEach { scanListeners.remove(it) } }
        }

        override fun onFail(errorCode: Int) {
            logHelper.withEnable { "scan onFail: errorCode: $errorCode" }
            notify(scanListeners) { it.onFail(errorCode) }
            onFinish()
        }
    }
    private val connectStateCallback = object : BtConnectStateListener {

        override fun onStateChange(oldState: Int, newState: Int) {
            handler.post {
                connectStateListeners.notifyListener { it.onStateChange(oldState, newState) }
            }
        }
    }
    val connectStateListeners = object : ARSHelper<BtConnectStateListener>() {}
    val connectListeners = object : ARSHelper<BtConnectListener>() {}
    internal val tempConnectListener = mutableListOf<BtConnectListener>()
    internal lateinit var config: BtConfig
    private val connectCallback = object : BtConnectListener {
        override fun onStart(device: BtDevice) {
            setCurrent(getConnector(device))
            notify(connectListeners) { it.onStart(device) }
        }

        override fun onConnectFail(device: BtDevice, reason: Int) {
            notify(connectListeners) { it.onConnectFail(device, reason) }
            clear()
        }

        override fun onConnectSuccess(device: BtDevice, gatt: BluetoothGatt) {
            notify(connectListeners) { it.onConnectSuccess(device, gatt) }
            clear()
        }

        private fun clear() {
            tempConnectListener.forEach { connectListeners.remove(it) }
        }
    }

    @SuppressLint("MissingPermission")
    private val delayStopScan = { stopScan() }


    internal fun <T> notify(helper: ARSHelper<T>, notify: (T) -> Unit) {
        handler.post { helper.notifyListener { notify.invoke(it) } }
    }

    fun init(context: Context, config: BtConfig? = null) {
        this.context = context.applicationContext
        initWorkThread()
        device = BtInstance(context)
        cancelable.add(bleScanner)
        setConfig(config ?: BtConfig())
        watchState()
    }

    fun setConfig(config: BtConfig): BluetoothHelper {
        this.config = config
        return this
    }

    private fun initWorkThread() {
        handlerThread = HandlerThread(HT_NAME)
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    private fun watchState() {
        device.getBtStateListeners().add { _, turning, available ->
            //蓝牙正在关闭时关闭所有的操作以保证状态正确
            if (!available && turning) {
                cancelable.forEach { it.cancel() }
                handler.removeCallbacks(delayStopScan)
            }
        }
    }

    /**
     * 当用户拒绝后，此方法会直接返回false
     *
     * 建议使用[openIntent]
     * @see openIntent
     *
     * @see isBtSupport
     * @see isBleSupport
     */
    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun open(): Boolean {
        return btAdapter?.enable() ?: false
    }

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun close(): Boolean {
        if (isOpen()) {
            return btAdapter?.disable() ?: false
        }
        return true
    }

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun isOpen() = device.isEnable()

    fun isBtSupport() = device.isBtSupport()
    fun isBleSupport() = device.isBleSupport()

    /**
     * @param scanListener 该listener会在[BtScanListener.onEnd]回调后自动被移除，否则应该使用[scanListeners]注册监听
     *
     * @see scanListeners
     */
    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startBleScan(
        timeout: Long = 0L,
        filter: MutableList<ScanFilter>? = null,
        settings: ScanSettings? = null,
        scanListener: BtScanListener? = null
    ) {
        if (scanListener != null) {
            scanListeners.add(scanListener)
            tempScanListeners.add(scanListener)
        }
        bleScanner.filter(filter).setting(settings).setScanListener(scanCallback).startScan()
        if (timeout > 0) {
            handler.postDelayed(delayStopScan, timeout)
        }
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    fun stopScan() {
        bleScanner.stopScan()
        handler.removeCallbacks(delayStopScan)
    }

    private fun setCurrent(connector: BleConnector) {
        currentConnector = connector
    }

    fun getCurrent() = currentConnector

    fun getConnector(btDevice: BtDevice): BleConnector {
        var connector = bleConnector[btDevice.mac]
        if (connector != null) {
            return connector
        }
        connector = BleConnector(btDevice).apply {
            stateListener = connectStateCallback
            connectListener = connectCallback
        }
        bleConnector[btDevice.mac] = connector
        cancelable.add(connector)
        return connector
    }

}