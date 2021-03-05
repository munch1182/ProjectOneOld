package com.munch.lib.bt

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
    //在ble模式下，该值只能是true
    var strict: Boolean = true
)

interface BtScanListener {

    fun onStart()

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION]
    )
    fun onScan(device: BtDevice)

    fun onEnd(device: MutableList<BtDevice>)
}

@SuppressLint("MissingPermission")
sealed class BtScanner {

    protected var listener: BtScanListener? = null
    protected var res: MutableList<BtDevice> = mutableListOf()
    protected var scanning = false

    /**
     * 处理统一逻辑，并将回调结果通过[listener]传出
     */
    protected var resScanCallBack = object : BtScanListener {
        override fun onStart() {
            res.clear()
            listener?.onStart()
        }

        /**
         * 使用一个监听在此处统一处理结果的list，而不是在系统回调中分别获取
         */
        override fun onScan(device: BtDevice) {
            if (!res.contains(device)) {
                res.add(device)
                listener?.onScan(device)
            }
        }

        override fun onEnd(device: MutableList<BtDevice>) {
            listener?.onEnd(device)
        }
    }

    fun isScanning(): Boolean {
        return scanning
    }

    class ClassicScanner : BtScanner() {

        private val classicBtReceiver by lazy { ClassicBtReceiver(BluetoothHelper.getInstance().context) }
        private var filters: MutableList<ScanFilter> = mutableListOf()

        override fun start(filters: MutableList<ScanFilter>?) {
            if (scanning) {
                return
            }
            scanning = true
            super.start(filters)
            this.filters.clear()
            if (filters != null) {
                this.filters.addAll(filters)
            }
            classicBtReceiver.add(resScanCallBack)
            classicBtReceiver.register()
            BluetoothHelper.getInstance().btAdapter.startDiscovery()
        }

        override fun stop() {
            if (!scanning) {
                return
            }
            scanning = false
            super.stop()
            BluetoothHelper.getInstance().btAdapter.cancelDiscovery()
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
                            t.onScan(BtDevice.from(device, rssi!!.toInt()))
                        } else {
                            kotlin.run out@{
                                filters.forEach {
                                    if (it.strict) {
                                        if (it.name != null && it.name != device.name) {
                                            return@forEach
                                        }
                                        if (it.mac != null && it.mac != device.address) {
                                            return@forEach
                                        }
                                    } else {
                                        if (it.name != null) {
                                            if (device.name == null) {
                                                return@forEach
                                            }
                                            if (!device.name.contains(it.name!!)) {
                                                return@forEach
                                            }
                                        }
                                        if (it.mac != null && !device.address.contains(it.mac!!)) {
                                            return@forEach
                                        }
                                    }
                                    t.onScan(BtDevice.from(device, rssi!!.toInt()))
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
                    resScanCallBack.onScan(BtDevice.from(result))
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
            if (scanning) {
                return
            }
            scanning = true
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
            if (!scanning) {
                return
            }
            scanning = false
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
        resScanCallBack.onStart()
    }

    /**
     * 当调用[start]时，扫描结束或失败时会自动调用[stop]
     * 手动调用会提前结束扫描
     *
     * 此方法调用了[BtScanListener.onEnd]，不需要在逻辑中调用
     */
    open fun stop() {
        resScanCallBack.onEnd(res)
    }

    /**
     * 设置扫描回调
     */
    open fun setScanListener(listener: BtScanListener? = null): BtScanner {
        this.listener = listener
        return this
    }
}

class BtScannerHelper(private val thread: Handler) : AddRemoveSetHelper<BtScanListener>() {

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
        override fun onScan(device: BtDevice) {
            getScanList().forEach { thread.post { it.onScan(device) } }
        }

        override fun onEnd(device: MutableList<BtDevice>) {
            getScanList().forEach { thread.post { it.onEnd(device) } }
            if (onceScanListener != null) {
                getScanList().remove(onceScanListener!!)
                onceScanListener = null
            }
        }
    }

    internal fun getScanList() = arrays
    private var onceScanListener: BtScanListener? = null

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

    /**
     * 开始扫描设备
     *
     * @param type 需要扫描的设备类型
     * @param scanFilter 扫描过滤，即只返回符合该集合中过滤条件的设备
     *                   注意：在ble模式下，过滤条件只能是严格
     * @param timeout 扫描最大时间，超过该时间则会停止，设为0则无超时时间限制
     * @param scanListener 该次扫描的回调，注意：该回调在此次扫描结束后即会被自动移除
     * 也可以使用 [add]来使用全局回调
     *
     * @see add
     * @see remove
     *
     */
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
            val scanner = when (type) {
                BtType.Classic -> getClassicScanner(this.scanListener)
                BtType.Ble -> getBleScanner(this.scanListener)
            }
            if (scanner.isScanning()) {
                return
            }
            if (scanListener != null) {
                onceScanListener = scanListener
                getScanList().add(onceScanListener!!)
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
        }
    }
}