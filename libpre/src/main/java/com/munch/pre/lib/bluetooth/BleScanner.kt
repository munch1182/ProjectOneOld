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
 * 内部没有维护scanning的状态，这是因为因为[ScanFailReason.SCAN_FAILED_ALREADY_STARTED]会回调失败
 *
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

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
            val device = BtDevice.from(result)
            if (!res.contains(device)) {
                res.add(device)
                scanListener?.onScan(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val code = ScanFailReason.fromErrorCode(errorCode)
            scanListener?.onFail(code)
            stopScan()
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
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
        val scanner = BluetoothHelper.INSTANCE.btAdapter?.bluetoothLeScanner
        if (scanner == null) {
            scanListener?.onFail(ScanFailReason.SCAN_FAILED_NO_SCANNER)
            return
        }
        res.clear()
        this.scanListener?.onStart()
        scanner.startScan(filter, settings ?: defScanSettingBuilder().build(), scanCallback)
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    fun stopScan() {
        val scanner = BluetoothHelper.INSTANCE.btAdapter?.bluetoothLeScanner ?: return
        scanner.stopScan(scanCallback)
        scanListener?.onEnd(res)
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    override fun cancel() {
        stopScan()
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN]
    )
    override fun destroy() {
        cancel()
        res.clear()
        scanListener = null
    }
}