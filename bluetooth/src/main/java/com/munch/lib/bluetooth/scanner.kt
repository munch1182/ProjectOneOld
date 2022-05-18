package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.munch.lib.RepeatStrategy

/**
 * Create by munch1182 on 2022/5/18 17:00.
 */
class ScanTarget {
    companion object {
        const val TIMEOUT = 10000L
    }

    /**
     * 用于寻找的特定设备地址
     *
     * 如果该值不为null
     * 1. 如果该值为标志的mac地址，则扫描将在扫到该设备后立即停止扫描并返回设备对象
     * 2. 否则，则扫描所有带有该字符的蓝牙设备
     */
    var mac: String? = null

    /**
     * 用于寻找名称包含该值的设备
     */
    var name: String? = null

    /**
     * 扫描的超时时间，ms
     */
    var timeout = TIMEOUT

    /**
     * 是否忽略没有名称的蓝牙设备
     */
    var isIgnoreNoName = true

    /**
     * 只返回信号小于该值的蓝牙设备
     */
    var rssi = Int.MAX_VALUE

    /**
     * 扫描时，如果正在扫描中，执行的策略
     */
    var repeatStrategy = RepeatStrategy.Ignore

    /**
     * 扫描设置，Ble限定
     */
    var scanSetting: ScanSettings = ScanSettings.Builder()
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
}

interface Scanner {

    /**
     * 启动蓝牙扫描
     *
     * @param target 用于扫描的目标
     * @param listener 扫描结果的回调
     * @return 是否启动成功
     */
    fun scan(target: ScanTarget, listener: ScanListener): Boolean

    fun stop(): Boolean
}

interface ScanListener {
    fun onDevScanned(dev: BluetoothDev)
}

@SuppressLint("MissingPermission")
internal class BleScanner : Scanner {

    private var helper: BluetoothHelper? = null
    private val scanner: BluetoothLeScanner?
        get() = helper?.adapter?.bluetoothLeScanner

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val dev = result?.let { BluetoothDev.from(result) } ?: return
            scanListener?.onDevScanned(dev)
        }
    }
    private var scanListener: ScanListener? = null

    override fun scan(target: ScanTarget, listener: ScanListener): Boolean {
        scanner ?: return false
        scanListener = listener
        scanner?.startScan(null, target.scanSetting, callback)
        return true
    }

    override fun stop(): Boolean {
        return true
    }

}