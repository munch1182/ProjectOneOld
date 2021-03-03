package com.munch.lib.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Handler
import androidx.annotation.RequiresPermission
import com.munch.lib.SYNCHRONIZED
import com.munch.lib.helper.AddRemoveSetHelper
import com.munch.lib.helper.ReceiverHelper
import android.bluetooth.le.ScanFilter as Filter

/**
 * Create by munch1182 on 2021/3/2 17:22.
 */
data class ScanFilter(
    var name: String? = null, var mac: String? = null,
    //是否严格匹配，即两个参数完全相同
    var strict: Boolean = true
)

interface BtScanListener {

    fun onStart()

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun onScan(device: BtDeviceBean)

    fun onEnd(device: MutableList<BtDeviceBean>)
}

@SuppressLint("MissingPermission")
sealed class BtScanner {

    protected var listener: BtScanListener? = null
    protected var res: MutableList<BtDeviceBean> = mutableListOf()
    protected var isScanning = false
    protected var resScanListener = object : BtScanListener {
        override fun onStart() {
            listener?.onStart()
        }

        /**
         * 使用一个监听在此处统一处理结果的list，而不是在系统回调中分别获取
         */
        override fun onScan(device: BtDeviceBean) {
            if (!res.contains(device)) {
                res.add(device)
                listener?.onScan(device)
            }
        }

        override fun onEnd(device: MutableList<BtDeviceBean>) {
            listener?.onEnd(device)
        }
    }

    class ClassicScanner : BtScanner() {

        private val classicBtReceiver by lazy { ClassicBtReceiver(BluetoothHelper.getInstance().context) }
        private var filters: MutableList<ScanFilter> = mutableListOf()

        override fun start(filters: MutableList<ScanFilter>?) {
            if (isScanning) {
                return
            }
            isScanning = true
            super.start(filters)
            this.filters.clear()
            if (filters != null) {
                this.filters.addAll(filters)
            }
            classicBtReceiver.add(resScanListener)
            classicBtReceiver.register()
            BluetoothAdapter.getDefaultAdapter().startDiscovery()
        }

        override fun stop() {
            if (!isScanning) {
                return
            }
            isScanning = false
            super.stop()
            classicBtReceiver.unregister()
        }

        inner class ClassicBtReceiver(context: Context) :
            ReceiverHelper<BtScanListener>(
                context,
                arrayOf(BluetoothAdapter.ACTION_DISCOVERY_FINISHED, BluetoothDevice.ACTION_FOUND)
            ) {

            override fun handleAction(
                action: String,
                context: Context?,
                intent: Intent,
                t: BtScanListener
            ) {
                when (action) {
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> stop()
                    BluetoothDevice.ACTION_FOUND -> {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                                ?: return
                        val rssi = intent.extras?.getShort(BluetoothDevice.EXTRA_RSSI, 0)
                        if (filters.isEmpty()) {
                            t.onScan(BtDeviceBean.from(device, rssi!!.toInt()))
                        } else {
                            kotlin.run out@{
                                filters.forEach {

                                    if (it.strict) {
                                        if (it.name != null && it.name != device.name) {
                                            return@forEach
                                        }
                                    } else {
                                        if (it.name != null && device.name != null
                                            && !device.name.contains(it.name!!)
                                        ) {
                                            return@forEach
                                        }
                                        if (it.mac != null && device.address != null
                                            && !device.address.contains(it.name!!)
                                        ) {
                                            return@forEach
                                        }
                                    }
                                    t.onScan(BtDeviceBean.from(device, rssi!!.toInt()))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class BleScanner : BtScanner() {

        private val callBack = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (result != null) {
                    resScanListener.onScan(BtDeviceBean.from(result))
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                stop()
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                super.onBatchScanResults(results)
                stop()
            }
        }

        override fun start(filters: MutableList<ScanFilter>?) {
            if (isScanning) {
                return
            }
            isScanning = true
            super.start(filters)
            val scanSettings = ScanSettings.Builder()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                //此处可以更改模式
                scanSettings.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                scanSettings.setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                scanSettings.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            }

            BluetoothHelper.getInstance().btAdapter.bluetoothLeScanner?.startScan(
                filters?.map {
                    val filterBuilder = Filter.Builder().setDeviceName(it.name)
                    try {
                        filterBuilder.setDeviceAddress(it.mac)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    filterBuilder.build()
                }, scanSettings.build(), callBack
            )
        }

        override fun stop() {
            if (!isScanning) {
                return
            }
            isScanning = false
            super.stop()
            BluetoothHelper.getInstance().btAdapter.bluetoothLeScanner?.stopScan(callBack)
        }
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    open fun start(filters: MutableList<ScanFilter>?) {
        listener?.onStart()
    }

    /**
     * 当调用[start]时，扫描结束或失败时会自动调用[stop]
     * 手动调用会提前结束扫描
     *
     * 此方法调用了[BtScanListener.onEnd]，不需要在逻辑中调用
     */
    open fun stop() {
        listener?.onEnd(res)
    }

    open fun setScanListener(listener: BtScanListener? = null): BtScanner {
        this.listener = listener
        return this
    }
}

class ScannerHelper(private val thread: Handler) : AddRemoveSetHelper<BtScanListener>() {

    private var classicScanner: BtScanner? = null
    private var bleScanner: BtScanner? = null
    private val scanListener = object : BtScanListener {
        override fun onStart() {
            getScanList().forEach { thread.post { it.onStart() } }
        }

        @RequiresPermission(
            allOf = [android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION]
        )
        override fun onScan(device: BtDeviceBean) {
            getScanList().forEach { thread.post { it.onScan(device) } }
        }

        override fun onEnd(device: MutableList<BtDeviceBean>) {
            getScanList().forEach { thread.post { it.onEnd(device) } }
        }
    }

    internal fun getScanList() = arrays
    private val onceScanListenerList = mutableListOf<BtScanListener>()

    private fun getClassicScanner(scanListener: BtScanListener): BtScanner {
        if (classicScanner == null) {
            classicScanner = BtScanner.ClassicScanner().setScanListener(scanListener)
        }
        return classicScanner!!
    }

    private fun getBleScanner(scanListener: BtScanListener): BtScanner {
        if (bleScanner == null) {
            bleScanner = BtScanner.BleScanner().setScanListener(scanListener)
        }
        return bleScanner!!
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    @SYNCHRONIZED
    fun startScan(
        type: BtType,
        scanFilter: MutableList<ScanFilter>? = null,
        timeout: Long = 0L,
        scanListener: BtScanListener?
    ) {
        synchronized(this) {
            if (scanListener != null) {
                onceScanListenerList.add(scanListener)
                getScanList().addAll(onceScanListenerList)
            }
            val scanner = when (type) {
                BtType.Classic -> getClassicScanner(this.scanListener)
                BtType.Ble -> getBleScanner(this.scanListener)
            }
            scanner.start(scanFilter)
            if (timeout > 0L) {
                thread.postDelayed({ stopScan() }, timeout)
            }
        }
    }

    @SYNCHRONIZED
    fun stopScan() {
        synchronized(this) {
            classicScanner?.stop()
            bleScanner?.stop()
            if (onceScanListenerList.isNotEmpty()) {
                getScanList().removeAll(onceScanListenerList)
                onceScanListenerList.clear()
            }
        }
    }
}