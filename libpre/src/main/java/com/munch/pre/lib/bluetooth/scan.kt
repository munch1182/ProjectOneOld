package com.munch.pre.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Parcelable
import androidx.annotation.RequiresPermission
import com.munch.pre.lib.SYNCHRONIZED
import com.munch.pre.lib.helper.ARSHelper
import com.munch.pre.lib.helper.receiver.ReceiverHelper
import kotlinx.parcelize.Parcelize
import android.bluetooth.le.ScanFilter as Filter

/**
 * Create by munch1182 on 2021/3/2 17:22.
 */
@Parcelize
data class ScanFilter(
    var name: String? = null, var mac: String? = null,
    //是否严格匹配，即两个参数完全相同
    //多个匹配时此值需要保持一致
    var strict: Boolean = true
) : Parcelable {
    @RequiresPermission(android.Manifest.permission.BLUETOOTH)
    fun isFiltered(device: BluetoothDevice): Boolean {
        if (strict) {
            return (name != null && name != device.name) || (mac != null && mac != device.address)
        } else {
            if (name != null && device.name?.contains(name!!) != true) {
                return true
            }
            if (mac != null && !device.address.contains(mac!!)) {
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is ScanFilter) {
            return other.name == name && other.mac == mac && other.strict == strict
        }
        return false
    }
}

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
internal sealed class BtScanner {

    protected var listener: BtScanListener? = null
    protected var res: MutableList<BtDevice> = mutableListOf()
    internal var scanning = false

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

    internal class ClassicScanner : BtScanner() {

        private val classicBtReceiver by lazy { ClassicBtReceiver(BluetoothHelper.INSTANCE.context) }
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
            BluetoothHelper.INSTANCE.btAdapter?.startDiscovery()
        }

        override fun stop() {
            if (!scanning) {
                return
            }
            scanning = false
            super.stop()
            BluetoothHelper.INSTANCE.btAdapter?.cancelDiscovery()
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
                            filters.forEach {
                                if (it.isFiltered(device)) {
                                    return@forEach
                                }
                                t.onScan(BtDevice.from(device, rssi!!.toInt()))
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    internal class BleScanner : BtScanner() {

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
            val isStrict = !filters.isNullOrEmpty() && filters[0].strict
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                scanSettings.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                scanSettings.setMatchMode(if (isStrict) ScanSettings.MATCH_MODE_STICKY else ScanSettings.MATCH_MODE_AGGRESSIVE)
            }
            //scan模式，此参数会影响扫描速度
            scanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            BluetoothHelper.INSTANCE.btAdapter?.bluetoothLeScanner?.startScan(
                filters?.map {
                    Filter.Builder().setDeviceName(it.name).setDeviceAddress(it.mac).build()
                }, scanSettings.build(), callBack
            )
        }

        override fun stop() {
            if (!scanning) {
                return
            }
            scanning = false
            super.stop()
            BluetoothHelper.INSTANCE.btAdapter?.bluetoothLeScanner?.stopScan(callBack)
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

class BtScannerHelper(private val handler: Handler) : ARSHelper<BtScanListener>() {

    private var classicScanner: BtScanner? = null
    private var bleScanner: BtScanner? = null
    private val scanListener = object : BtScanListener {
        override fun onStart() {
            getScanList().forEach { handler.post { it.onStart() } }
        }

        @RequiresPermission(
            allOf = [android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION]
        )
        override fun onScan(device: BtDevice) {
            getScanList().forEach { handler.post { it.onScan(device) } }
        }

        override fun onEnd(device: MutableList<BtDevice>) {
            getScanList().forEach { handler.post { it.onEnd(device) } }
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
        timeout: Long,
        scanFilter: MutableList<ScanFilter>?,
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
                handler.postDelayed({ stopScan() }, timeout)
            }
        }
    }

    @SYNCHRONIZED
    fun stopScan() {
        synchronized(this) {
            if (classicScanner?.isScanning() == true) {
                classicScanner?.stop()
            }
            if (bleScanner?.isScanning() == true) {
                bleScanner?.stop()
            }
        }
    }

    fun resetState() {
        bleScanner?.let { it.scanning = false }
        classicScanner?.let { it.scanning = false }
    }
}