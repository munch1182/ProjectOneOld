package com.munch.lib.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.annotation.RequiresPermission
import com.munch.lib.base.Cancelable

/**
 * Create by munch1182 on 2021/8/24 13:57.
 */
interface Scanner : Cancelable {

    fun start()

    fun stop()

    override fun cancel() {
        stop()
    }
}

interface OnScannerListener {

    fun onStart()
    fun onScan(device: BtDevice)
    fun onBatchScan(devices: MutableList<BtDevice>)
    fun onComplete(devices: MutableList<BtDevice>)
    fun onFail()
}

data class ScanFilter(val name: String?, val mac: String?)

class ClassicScanner : Scanner {

    override fun start() {
    }

    override fun stop() {
    }

}

class BleScanner : Scanner {

    internal var listener: OnScannerListener? = null
    private val scanner: BluetoothLeScanner?
        get() = BluetoothHelper.instance.set.adapter?.bluetoothLeScanner
    private val scannedDevs = mutableListOf<BtDevice>()
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            BluetoothHelper.logSystem.withEnable { "onScanResult:$callbackType, ${result?.device?.name ?: "null"}(${result?.device?.address ?: "null"})" }
            result ?: return
            val device = BtDevice.from(result.device, BluetoothType.Ble, result.rssi)
            if (!scannedDevs.contains(device)) {
                scannedDevs.add(device)
                listener?.onScan(device)
            }
        }

        /**
         * 批量返回
         */
        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            val list = results?.map { BtDevice.from(it.device, BluetoothType.Ble, it.rssi) }
                ?.filter { !scannedDevs.contains(it) }
            BluetoothHelper.logSystem.withEnable { "onBatchScanResults:${results?.size ?: 0} -> ${list?.size ?: 0}" }
            list ?: return
            if (list.isNotEmpty()) {
                scannedDevs.addAll(list)
                listener?.onBatchScan(list.toMutableList())
            }
        }

        @SuppressLint("MissingPermission")
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            BluetoothHelper.logSystem.withEnable { "onScanFailed:$errorCode" }
            stop()
        }
    }

    @SuppressLint("MissingPermission")
    private val delay2Stop = Runnable {
        BluetoothHelper.logHelper.withEnable { "timeout to stop scan" }
        stop()
    }
    internal var builder: Builder? = null

    @RequiresPermission(
        allOf = [Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION]
    )
    override fun start() {
        BluetoothHelper.logHelper.withEnable { "start scan" }
        scannedDevs.clear()
        if (builder != null && builder!!.timeout > 0L) {
            delayStop(builder!!.timeout)
        }
        listener?.onStart()
        scanner?.startScan(builder?.filter, sureSettings(), scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    private fun delayStop(timeout: Long) {
        BluetoothHelper.instance.workHandler.postDelayed(delay2Stop, timeout)
    }

    private fun sureSettings(): ScanSettings {
        return builder?.settings ?: ScanSettings.Builder()
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            //批量扫描
            .apply {
                if (builder != null) {
                    if (BluetoothHelper.instance.adapter?.isOffloadedScanBatchingSupported == true) {
                        setReportDelay(builder!!.reportDelay)
                    }
                }
            }
            .build()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    override fun stop() {
        BluetoothHelper.logHelper.withEnable { "stop scan" }
        BluetoothHelper.instance.workHandler.removeCallbacks(delay2Stop)
        //此方法不会触发回调
        scanner?.stopScan(scanCallback)
        //因此主动触发
        if (BluetoothHelper.instance.state.isScanning) {
            listener?.onComplete(scannedDevs)
            BluetoothHelper.instance.state.currentStateVal = BluetoothState.IDLE
        }
    }

    class Builder internal constructor() {
        internal var filter: List<android.bluetooth.le.ScanFilter>? = null
        internal var settings: ScanSettings? = null
        internal var timeout = 35 * 1000L
        internal var reportDelay = 0L

        fun setFilter(filter: MutableList<ScanFilter>?): Builder {
            this.filter = filter?.map {
                android.bluetooth.le.ScanFilter.Builder()
                    .apply {
                        if (BluetoothHelper.checkMac(it.mac)) {
                            setDeviceAddress(it.mac)
                        }
                        if (it.name != null && it.name.isNotEmpty()) {
                            setDeviceName(it.name)
                        }
                    }
                    .build()
            }
            return this
        }

        fun setSettings(settings: ScanSettings): Builder {
            this.settings = settings
            return this
        }

        fun setTimeout(timeout: Long): Builder {
            this.timeout = timeout
            return this
        }

        fun setReportDelay(reportDelay: Long = 0L): Builder {
            this.reportDelay = reportDelay
            return this
        }

        @RequiresPermission(
            allOf = [Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION]
        )
        fun startScan() {
            BluetoothHelper.instance.startBleScan()
        }
    }
}