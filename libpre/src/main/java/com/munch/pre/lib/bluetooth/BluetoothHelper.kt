package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.helper.ARSHelper

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
    }

    internal lateinit var context: Context
    internal lateinit var device: BtInstance
    internal val btAdapter: BluetoothAdapter? by lazy { device.btAdapter }
    internal var btConfig: BtConfig? = null
    private val cancelable = mutableListOf<Cancelable>()
    private lateinit var handlerThread: HandlerThread
    internal lateinit var handler: Handler
    private var current: BleDevice? = null

    private val bleScanner = BleScanner()
    private val scanListeners = object : ARSHelper<BtScanListener>() {}
    private val tempScanListeners = mutableListOf<BtScanListener>()
    private val scanCallback = object : BtScanListener {
        override fun onStart() {
            handler.post { scanListeners.notifyListener { it.onStart() } }
        }

        override fun onScan(device: BtDevice) {
            handler.post { scanListeners.notifyListener { it.onScan(device) } }
        }

        override fun onEnd(devices: MutableList<BtDevice>) {
            handler.post { scanListeners.notifyListener { it.onEnd(devices) } }
        }

        override fun onFail(errorCode: Int) {
            handler.post {
                scanListeners.notifyListener { it.onFail(errorCode) }
                tempScanListeners.forEach { scanListeners.remove(it) }
            }
        }
    }

    private val connectStateCallback = object : BtConnectStateListener {
        override fun onStateChange(newState: Int) {
            handler.post {
                connectStateListeners.notifyListener { it.onStateChange(newState) }
            }
        }

        override fun onStateChange(oldState: Int, newState: Int) {
            super.onStateChange(oldState, newState)
            handler.post {
                connectStateListeners.notifyListener { it.onStateChange(oldState, newState) }
            }
        }
    }
    private val connectStateListeners = object : ARSHelper<BtConnectStateListener>() {}

    fun init(context: Context, config: BtConfig? = null) {
        this.context = context.applicationContext
        initWorkThread()
        device = BtInstance(context)
        setConfig(config)
        cancelable.add(bleScanner)
        watchState()
    }

    private fun initWorkThread() {
        handlerThread = HandlerThread(HT_NAME)
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    private fun watchState() {
        device.getBtStateListeners().add { _, turning, available ->
            //蓝牙正在关闭时关闭所有的操作以保证状态正确
            //@see resetState
            if (!available && turning) {
                cancelable.forEach { it.cancel() }
            }
        }
    }

    fun setConfig(btConfig: BtConfig?): BluetoothHelper {
        if (btConfig != this.btConfig) {
            this.btConfig = btConfig
        }
        return this
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

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startClassicScan(
        timeout: Long = 0L,
        scanListener: BtScanListener? = null
    ) {

    }

    /**
     * @param scanListener 该listener会在[BtScanListener.onEnd]回调后自动被移除，否则应该使用[getScanListeners]注册监听
     *
     * @see getScanListeners
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
            handler.postDelayed({ stopScan() }, timeout)
        }
    }

    fun getScanListeners() = scanListeners

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    fun stopScan() {
        bleScanner.stopScan()
    }

    fun setCurrent(connector: BleDevice) {
        current = connector

        current!!.stateListener = connectStateCallback
    }

    fun getCurrent() = current

}