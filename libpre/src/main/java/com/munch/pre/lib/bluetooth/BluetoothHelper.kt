package com.munch.pre.lib.bluetooth

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

        private const val HT_NAME = "BLUETOOTH_HANDLER_THREAD"
    }

    internal lateinit var context: Context
    internal lateinit var ht: HandlerThread
    internal lateinit var handler: Handler
    internal lateinit var device: BtDeviceInstance
    internal val btAdapter by lazy { device.btAdapter }
    internal lateinit var scanner: BtScannerHelper
    internal var btConfig: BtConfig? = null

    fun init(context: Context, config: BtConfig? = null) {
        this.context = context.applicationContext
        initWorkThread()
        device = BtDeviceInstance(context)
        scanner = BtScannerHelper(handler)
        setConfig(config)
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

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun open(): Boolean {
        if (!isOpen()) {
            return btAdapter.enable()
        }
        return true
    }

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun close(): Boolean {
        if (isOpen()) {
            return btAdapter.disable()
        }
        return true
    }

    @RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN])
    fun isOpen() = device.isEnable()

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
    }
}