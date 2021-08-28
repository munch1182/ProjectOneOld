package com.munch.lib.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.munch.lib.base.Cancelable
import com.munch.lib.helper.receiver.ReceiverHelper

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
    fun onScan(device: BluetoothDev)
    fun onBatchScan(devices: MutableList<BluetoothDev>)
    fun onComplete(devices: MutableList<BluetoothDev>)
    fun onFail()
}

data class ScanFilter(val name: String?, val mac: String?)

class ScannerBuilder internal constructor(private val type: BluetoothType) {
    internal var filter: List<android.bluetooth.le.ScanFilter>? = null
    internal var settings: ScanSettings? = null
    internal var timeout = 35 * 1000L
    internal var reportDelay = 0L
    internal var justFirst = true

    fun setFilter(filter: MutableList<ScanFilter>?): ScannerBuilder {
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

    fun setSettings(settings: ScanSettings): ScannerBuilder {
        this.settings = settings
        return this
    }

    fun setTimeout(timeout: Long): ScannerBuilder {
        this.timeout = timeout
        return this
    }

    fun setReportDelay(reportDelay: Long = 0L): ScannerBuilder {
        this.reportDelay = reportDelay
        return this
    }

    /**
     * 设备是否只返回第一次出现设备，即是否重复回调相同的设备
     *
     * 相同的设备的多次回调信息(比如信号强度)会更新
     */
    fun setJustFirst(justFirst: Boolean = true): ScannerBuilder {
        this.justFirst = justFirst
        return this
    }

    /**
     * 不同的类型需要不同的权限
     */
    fun startScan() {
        BluetoothHelper.instance.startScan(type, this)
    }

    override fun toString(): String {
        return "ScannerBuilder(type=$type, filter=$filter, settings=$settings, timeout=$timeout, reportDelay=$reportDelay)"
    }

}

internal class ClassicScanner(context: Context) : Scanner {

    private val receiver = BluetoothDiscoveryReceiver(context)
    internal var builder: ScannerBuilder? = null
    internal var listener: OnScannerListener? = null
    private val scanCallback = object : OnScannerListener {
        override fun onStart() {
            listener?.onStart()
        }

        override fun onScan(device: BluetoothDev) {
            listener?.onScan(device)
        }

        override fun onBatchScan(devices: MutableList<BluetoothDev>) {
            listener?.onBatchScan(devices)
        }

        override fun onComplete(devices: MutableList<BluetoothDev>) {
            BluetoothHelper.instance.workHandler.removeCallbacks(delay2Stop)
            listener?.onComplete(devices)
            listener = null
            receiver.remove(this)
            receiver.unregister()
        }

        override fun onFail() {
            listener?.onFail()
        }
    }
    private val delay2Stop = Runnable {
        BluetoothHelper.logHelper.withEnable { "timeout to stop scan" }
        stop()
    }

    private fun delayStop(timeout: Long) {
        BluetoothHelper.instance.workHandler.postDelayed(delay2Stop, timeout)
    }

    @SuppressLint("MissingPermission")
    override fun start() {
        BluetoothHelper.logHelper.withEnable { "start classic scan" }
        receiver.add(scanCallback)
        receiver.apply { scanBuilder = builder }.register()
        if (builder != null && builder!!.timeout > 0L) {
            delayStop(builder!!.timeout)
        }
        BluetoothHelper.instance.adapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun stop() {
        val instance = BluetoothHelper.instance
        BluetoothHelper.logHelper.withEnable { "stop classic scan" }
        instance.adapter?.cancelDiscovery()
        //通知搜索后，应在回调中更改状态
    }
}

internal class BleScanner : Scanner {

    internal var listener: OnScannerListener? = null
    private val scanner: BluetoothLeScanner?
        get() = BluetoothHelper.instance.set.adapter?.bluetoothLeScanner
    private val scannedDevs = mutableListOf<BluetoothDev>()
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            /*BluetoothHelper.logSystem.withEnable {
                "onScanResult:$callbackType, ${result?.device?.name ?: "null"}(${result?.device?.address ?: "null"})"
            }*/
            result ?: return
            val device = BluetoothDev.from(result)
            if (builder?.justFirst == true) {
                if (!scannedDevs.contains(device)) {
                    listener?.onScan(device)
                    scannedDevs.add(device)
                }
            } else {
                listener?.onScan(device)
                scannedDevs.add(device)
            }
        }

        /**
         * 批量返回
         */
        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            val list = results?.map { BluetoothDev.from(it) }
                ?.filter { builder?.justFirst == false || !scannedDevs.contains(it) }
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
    internal var builder: ScannerBuilder? = null

    @RequiresPermission(
        allOf = [Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION]
    )
    override fun start() {
        BluetoothHelper.logHelper.withEnable { "start ble scan" }
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
        BluetoothHelper.logHelper.withEnable { "stop ble scan" }
        val instance = BluetoothHelper.instance
        instance.workHandler.removeCallbacks(delay2Stop)
        //此方法不会触发回调
        scanner?.stopScan(scanCallback)
        //因此主动触发
        listener?.onComplete(scannedDevs)
        listener = null
        instance.state.currentStateVal = BluetoothState.IDLE
    }
}

class BluetoothDiscoveryReceiver(context: Context) : ReceiverHelper<OnScannerListener>(
    context, arrayOf(
        BluetoothAdapter.ACTION_DISCOVERY_STARTED,
        BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
        BluetoothDevice.ACTION_FOUND
    )
) {

    private val devs = mutableListOf<BluetoothDev>()

    var scanBuilder: ScannerBuilder? = null

    @SuppressLint("MissingPermission")
    override fun handleAction(
        action: String,
        context: Context?,
        intent: Intent,
        t: OnScannerListener
    ) {
        when (action) {
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                devs.clear()
                t.onStart()
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                t.onComplete(devs)
                remove(t)
            }
            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return
                if (!isValid(device)) return
                val rssi =
                    intent.extras?.getShort(BluetoothDevice.EXTRA_RSSI, 0.toShort())?.toInt() ?: 0
                val dev = BluetoothDev.from(device, rssi)
                if (scanBuilder?.justFirst == true && devs.contains(dev)) {
                    return
                }
                devs.add(dev)
                t.onScan(dev)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun isValid(device: BluetoothDevice): Boolean {
        val filters = scanBuilder?.filter?.takeIf { it.isNotEmpty() } ?: return true
        filters.forEach {
            if (((it.deviceName == null || it.deviceName == device.name)
                        && (it.deviceAddress == null || it.deviceAddress == device.address))
            ) {
                return true
            }
        }
        return false
    }
}