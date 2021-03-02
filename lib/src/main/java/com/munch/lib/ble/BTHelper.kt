package com.munch.lib.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.munch.lib.RequiresPermission as Permission

/**
 * Create by munch1182 on 2021/3/2 16:55.
 */
@Permission(
    anyOf = [
        //蓝牙权限
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        //蓝牙扫描权限
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.BLUETOOTH_PRIVILEGED]
)
@SuppressLint("MissingPermission")
class BTHelper private constructor() {

    companion object {
        private val INSTANCE by lazy { BTHelper() }

        fun getInstance() = INSTANCE
    }

    internal lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    private val btAdapter = BluetoothAdapter.getDefaultAdapter()

    /**
     * 该设备是否支持蓝牙，即使用的设备是否有蓝牙模块
     */
    fun isBtSupport() = btAdapter != null

    /**
     * 蓝牙是否可用，即蓝牙是否打开
     */
    fun isEnable(): Boolean {
        return btAdapter?.isEnabled ?: false
    }

    fun open() {
        if (!isEnable()) {
            btAdapter.enable()
        }
    }

    fun close() {
        if (isEnable()) {
            btAdapter.disable()
        }
    }

    fun startScan(
        type: BtType,
        scanFilter: MutableList<ScanFilter> = mutableListOf(),
        scanListener: BtScanListener? = null
    ) {
        val scanner = when (type) {
            BtType.Classic -> {
                BtScanner.ClassicScanner()
            }
            BtType.Ble -> {
                BtScanner.BleScanner()
            }
        }
        scanner.setScanListener(scanListener).start(scanFilter)
    }

    fun startClassicScan(
        scanFilter: MutableList<ScanFilter> = mutableListOf(),
        scanListener: BtScanListener? = null
    ) {
        startScan(BtType.Classic, scanFilter, scanListener)
    }

    fun startBleScan(
        scanFilter: MutableList<ScanFilter> = mutableListOf(),
        scanListener: BtScanListener? = null
    ) {
        startScan(BtType.Ble, scanFilter, scanListener)
    }
}