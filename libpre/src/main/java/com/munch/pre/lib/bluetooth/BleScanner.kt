package com.munch.pre.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import androidx.annotation.RequiresPermission
import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.base.Destroyable

/**
 * Create by munch1182 on 2021/4/26 14:59.
 */
class BleScanner : Cancelable, Destroyable {

    companion object {
        fun defScanSettingBuilder(): ScanSettings.Builder {
            return ScanSettings.Builder()
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                    }
                    setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                }
        }
    }

    private val res = mutableListOf<BtDevice>()
    private var filter: MutableList<ScanFilter>? = null
    private var settings: ScanSettings? = null
    private var scanning = false
        get() = synchronized(this) { field }
        set(value) = synchronized(this) {
            field = value
            BluetoothHelper.logHelper.withEnable { "scanning change: $value" }
        }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            BluetoothHelper.logSystem.withEnable { "onScanResult: ${result?.device?.address ?: "null"}" }
            result ?: return
            val device = BtDevice.from(result)
            if (!res.contains(device)) {
                //因为BtScanListener.onEnd时收到调用，所以会先于bluetoothLeScanner?.stopScan调用
                if (!scanning) {
                    return
                }
                res.add(device)
                scanListener?.onScan(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            BluetoothHelper.logSystem.withEnable { "onScanFailed: errorCode: $errorCode" }
            val code = ScanFailReason.fromErrorCode(errorCode)
            scanListener?.onFail(code)
            stopScan()
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            BluetoothHelper.logSystem.withEnable { "onBatchScanResults: results: ${results?.size ?: 0}" }
            stopScan()
        }
    }
    private var scanListener: BtScanListener? = null

    fun filter(filter: MutableList<ScanFilter>?): BleScanner {
        this.filter = filter
        return this
    }

    fun setting(settings: ScanSettings?): BleScanner {
        this.settings = settings
        return this
    }

    fun setScanListener(scanListener: BtScanListener): BleScanner {
        this.scanListener = scanListener
        return this
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun startScan() {
        if (scanning) {
            return
        }
        scanning = true
        val scanner = BluetoothHelper.INSTANCE.btAdapter?.bluetoothLeScanner
        if (scanner == null) {
            scanListener?.onFail(ScanFailReason.SCAN_FAILED_NO_SCANNER)
            return
        }
        res.clear()
        this.scanListener?.onStart()
        scanner.startScan(filter, getScanSettings(), scanCallback)
    }

    fun getScanSettings() = settings ?: defScanSettingBuilder().build()

    fun getFilter() = filter

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    fun stopScan() {
        if (!scanning) {
            return
        }
        scanning = false
        //当蓝牙关闭时，bluetoothLeScanner为null
        BluetoothHelper.INSTANCE.btAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        scanListener?.onEnd(res)
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    override fun cancel() {
        BluetoothHelper.logHelper.withEnable { "scanner cancel" }
        stopScan()
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    override fun destroy() {
        BluetoothHelper.logHelper.withEnable { "scanner destroy" }
        cancel()
        res.clear()
        scanListener = null
    }
}