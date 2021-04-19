package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import androidx.annotation.RequiresPermission

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

        private const val HT_NAME = "BLUETOOTH_HANDLER_THREAD"
    }

    internal lateinit var context: Context
    internal lateinit var ht: HandlerThread
    internal lateinit var handler: Handler
    internal lateinit var device: BtDeviceInstance
    internal val btAdapter: BluetoothAdapter? by lazy { device.btAdapter }
    internal lateinit var scanner: BtScannerHelper
    internal lateinit var connector: BtConnectorHelper
    internal var btConfig: BtConfig? = null

    fun init(context: Context, config: BtConfig? = null) {
        this.context = context.applicationContext
        initWorkThread()
        device = BtDeviceInstance(context)
        scanner = BtScannerHelper(handler)
        connector = BtConnectorHelper(handler)
        setConfig(config)
        watchState()
    }

    private fun watchState() {
        device.getBtStateListeners().add { _, turning, available ->
            //蓝牙正在关闭时关闭所有的操作以保证状态正确
            //@see resetState
            if (!available && turning) {
                scanner.stopScan()
                connector.disconnectNow()
            }
        }
    }

    fun setConfig(btConfig: BtConfig?): BluetoothHelper {
        if (btConfig != this.btConfig) {
            this.btConfig = btConfig
        }
        return this
    }

    private fun initWorkThread() {
        ht = HandlerThread(HT_NAME)
        ht.start()
        handler = Handler(ht.looper)
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
    fun startScan(
        type: BtType,
        timeout: Long = 0L,
        scanFilter: MutableList<ScanFilter>? = null,
        scanListener: BtScanListener? = null
    ) {
        scanner.startScan(type, timeout, scanFilter, scanListener)
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startClassicScan(
        timeout: Long = 0L,
        scanFilter: MutableList<ScanFilter>? = null,
        scanListener: BtScanListener? = null
    ) {
        startScan(BtType.Classic, timeout, scanFilter, scanListener)
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startBleScan(
        timeout: Long = 0L,
        scanFilter: MutableList<ScanFilter>? = null,
        scanListener: BtScanListener? = null
    ) {
        startScan(BtType.Ble, timeout, scanFilter, scanListener)
    }

    fun stopScan() {
        scanner.stopScan()
    }

    fun connect(device: BtDevice) {
        connector.connect(device)
    }

    fun disconnect() {
        connector.disconnectNow()
    }

    fun getConnectListeners() = connector.getConnectListeners()
    fun getStateListeners() = connector.getStateListeners()

    fun getCurrent() = connector.getCurrent()

    /**
     * 当因为手动关闭蓝牙状态出错时，手动更新状态
     *
     * 完成时此方法应该隐藏，但是考虑到蓝牙设备的复杂性，此方法可以保留
     */
    fun resetState() {
        connector.resetState()
        scanner.resetState()
    }

    data class Current(val device: BtDevice?, @ConnectState val state: Int)
}