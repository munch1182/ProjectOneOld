package com.munch.lib.bluetooth.scan

import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper

/**
 * Create by munch1182 on 2021/12/4 17:22.
 */
class BleScanner(
    private val p: ScanParameter.BleScanParameter,
    private val listener: OnScannerListener
) : ScanCallback(), IScanner {

    private val leScanner: BluetoothLeScanner
        get() = BluetoothHelper.instance.bluetoothEnv.adapter?.bluetoothLeScanner
            ?: throw IllegalStateException("can not get ble scanner")

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    override fun start() {
        super.start()
        val scanner = leScanner
        val list = p.target?.mapNotNull { it.convert2ScanFilter() } ?: arrayListOf()
        scanner.startScan(list, p.bleScanSet, this)
    }

    private fun ScanParameter.Target.convert2ScanFilter(): ScanFilter? {
        if (this.invalid || !exact) {
            return null
        }
        return ScanFilter.Builder().apply {
            name?.let { setDeviceName(it) }
            mac?.let { setDeviceAddress(it) }
        }.build()
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        val dev = result?.let { BluetoothDev.from(it) } ?: return
        listener.onDeviceScanned(dev)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        val devs = results?.map { BluetoothDev.from(it) }?.toTypedArray() ?: return
        listener.onBatchDeviceScanned(devs)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        val logSystem = BluetoothHelper.logSystem
        when (errorCode) {
            SCAN_FAILED_ALREADY_STARTED -> logSystem.log("scan fail: SCAN_FAILED_ALREADY_STARTED.")
            SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> logSystem.log("scan fail: SCAN_FAILED_APPLICATION_REGISTRATION_FAILED.")
            SCAN_FAILED_INTERNAL_ERROR -> logSystem.log("scan fail: SCAN_FAILED_INTERNAL_ERROR.")
            SCAN_FAILED_FEATURE_UNSUPPORTED -> logSystem.log("scan fail: SCAN_FAILED_FEATURE_UNSUPPORTED.")
            5 -> logSystem.log("scan fail: SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES.")
            6 -> logSystem.log("scan fail: SCAN_FAILED_SCANNING_TOO_FREQUENTLY.")
            else -> logSystem.log("scan fail: $errorCode.")
        }
        listener.onScanFail()
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    override fun stop() {
        super.stop()
        leScanner.stopScan(this)
        listener.onScanComplete()
    }
}