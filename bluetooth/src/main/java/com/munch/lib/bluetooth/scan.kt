package com.munch.lib.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import com.munch.lib.OnReceive
import com.munch.lib.extend.catch
import com.munch.lib.extend.lockWith
import com.munch.lib.helper.ARSHelper
import com.munch.lib.log.LogStyle
import com.munch.lib.log.Logger
import com.munch.lib.receiver.ReceiverHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex

/**
 * 设备过滤接口
 *
 * 因为日志需要打印, 所以实现类需要重写[toString]
 */
fun interface OnDeviceFilter {

    /**
     * 对设备进行过滤
     *
     * @return 设备是否需要被过滤, 如果返回true, 则该设备会被过滤掉, 不会回调到结果中
     */
    fun isDeviceNeedFilter(dev: IBluetoothDev): Boolean
}

/**
 * 不过滤任何设备
 */
object OnDeviceNoneFilter : OnDeviceFilter {
    override fun isDeviceNeedFilter(dev: IBluetoothDev) = false

    override fun toString() = "None"
}

/**
 * 设备扫描回调
 */
interface OnDeviceScanListener {
    fun onDeviceScanStart() {}
    fun onDeviceScanned(dev: BluetoothScanDev)
    fun onDeviceScanComplete() {}
}

/**
 * 扫描相关方法和状态
 */
interface ScannerFun {

    /**
     * 扫描蓝牙设备
     *
     * @param filter 对扫描到的设备进行过滤, 被过滤的设备不会回调到结果中
     * @param listener 扫描回调, 当扫描结束时, 此回调会被移除
     */
    fun startScan(
        filter: OnDeviceFilter = OnDeviceNoneFilter,
        listener: OnDeviceScanListener? = null
    )

    fun stopScan()

    val isScanning: Boolean
}

/**
 * 扫描状态分发
 */
interface ScanStateDispatcher {
    fun addDeviceScanListener(listener: OnDeviceScanListener)
    fun removeDeviceScanListener(listener: OnDeviceScanListener)
}

/**
 * 扫描相关功能
 */
interface Scanner : ScannerFun, ScanStateDispatcher {
    fun setScanType(type: BluetoothType): Scanner

    /**
     * 设置扫描超时时间
     *
     * 一次设置, 一直有效
     *
     * 如果要取消设置, timeout需要设置为0
     *
     * @param timeout 超时时间, 单位: ms
     */
    fun setScanTimeout(timeout: Long): Scanner

    fun cancelScanTimeout() = setScanTimeout(0)
}

/**
 * 实现扫描相关功能
 * 1. 分发扫描类型
 * 2. 分发扫描回调
 * 3. 实现超时取消
 */
object DispatchScanner : Scanner, BluetoothHelperFun, BluetoothHelperEnv,
    ARSHelper<OnDeviceScanListener?>() {

    private const val TIMEOUT_MIN = 10000L

    private var timeoutJob: Job? = null
    private var timeout = 0L
    private var scanner: ScannerFun? = null
    private var onceScanListener: OnDeviceScanListener? = null
    private val callback = object : OnDeviceScanListener {
        override fun onDeviceScanStart() {
            super.onDeviceScanStart()
            notifyUpdate { it?.onDeviceScanStart() }
            onceScanListener?.onDeviceScanStart()
        }

        override fun onDeviceScanned(dev: BluetoothScanDev) {
            notifyUpdate { it?.onDeviceScanned(dev) }
            onceScanListener?.onDeviceScanned(dev)
        }

        override fun onDeviceScanComplete() {
            super.onDeviceScanComplete()
            notifyUpdate { it?.onDeviceScanComplete() }
            onceScanListener?.onDeviceScanComplete()
            onceScanListener = null
        }
    }

    override fun setScanType(type: BluetoothType): Scanner {
        if (scanner != null) {
            return if (scanner is CLASSICScanner && type == BluetoothType.CLASSIC) {
                this
            } else if (scanner is LEScanner && type == BluetoothType.LE) {
                this
            } else {
                throw IllegalStateException("setScanType(${type}) but still have scanner(${scanner?.javaClass?.simpleName})")
            }
        }
        scanner = when (type) {
            BluetoothType.CLASSIC -> CLASSICScanner(env)
            BluetoothType.LE -> LEScanner(env)
            else -> throw IllegalStateException("unsupported: $type")
        }
        return this
    }

    override fun setScanTimeout(timeout: Long): Scanner {
        this.timeout = if (timeout < TIMEOUT_MIN) TIMEOUT_MIN else timeout
        return this
    }

    override val isScanning: Boolean
        get() = scanner?.isScanning ?: false

    override fun startScan(filter: OnDeviceFilter, listener: OnDeviceScanListener?) {
        onceScanListener = listener
        if (timeout > 0L) {
            setTimeoutCancel()
        }
        //统一回调, 避免先设置导致scanner为null而不回调
        scanner?.startScan(filter, callback)
    }

    private fun setTimeoutCancel() {
        timeoutJob = SupervisorJob()
        launch(timeoutJob!!) {
            log.log { "SCANNER set timeout for scanning: ${timeout}ms." }
            delay(timeout)
            if (isScanning) {
                log.log { "SCANNER scan timeout(${timeout}ms)." }
                stopScan()
            }
            timeoutJob?.cancel()
        }
    }

    override fun stopScan() {
        scanner?.stopScan()
        timeoutJob?.cancel()
        scanner = null
        timeoutJob = null
    }

    override fun addDeviceScanListener(listener: OnDeviceScanListener) {
        add(listener)
    }

    override fun removeDeviceScanListener(listener: OnDeviceScanListener) {
        remove(listener)
    }
}

/**
 * LE扫描方法实现
 * 处理了相关状态, 并统一回调为OnDeviceScanListener
 */
class LEScanner(private val env: IBluetoothEnv) : ScannerFun, BluetoothHelperFun {

    private val lock = Mutex()
    private var _isScanning = false
        get() = lock.lockWith(coroutineContext) { field }
        set(value) = lock.lockWith(coroutineContext) {
            val old = field
            field = value
            log.log { "LE SCAN STATE change: $old -> $value." }
            if (value) {
                scanListener?.onDeviceScanStart()
            } else {
                scanListener?.onDeviceScanComplete()
            }
        }

    override val isScanning: Boolean
        get() = _isScanning

    private var scanListener: OnDeviceScanListener? = null
    private var filter: OnDeviceFilter? = null

    private val callback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            launch {
                if (!_isScanning) return@launch
                _isScanning = false
                log.log { "SCAN fail: $errorCode." }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            launch {
                val dev = BluetoothScanDev(result ?: return@launch, BluetoothType.LE)
                // 手动停止接收后即不再接收结果
                if (!_isScanning) return@launch
                //过滤
                val isFilter = filter?.isDeviceNeedFilter(dev) ?: false
                log.log { "SCANNED: ${if (isFilter) "$dev Filtered" else "$dev"}." }
                if (!isFilter) {
                    scanListener?.onDeviceScanned(dev)
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            launch {
                if (!_isScanning) return@launch
                // 相当于不支持onBatchScanResults
                results?.forEach { onScanResult(0, it) }
            }
        }
    }

    override fun startScan(filter: OnDeviceFilter, listener: OnDeviceScanListener?) {
        val scanner = env.adapter?.bluetoothLeScanner
        if (scanner == null) {
            log.log { "error as null scanner." }
            return
        }
        if (_isScanning) {
            log.log { "call startScan() but is Scanning." }
            return
        }
        this.scanListener = listener
        this.filter = filter
        val setting = env.bleScanSetting
        log.log { "start LE SCAN(${setting.fmt()})." }
        log.log { "LE SCAN filter: ($filter)." }
        _isScanning = true

        scanner.startScan(null, setting, callback)
    }

    override fun stopScan() {
        val scanner = env.adapter?.bluetoothLeScanner
        if (scanner == null) {
            log.log { "error as null scanner." }
            return
        }
        if (!_isScanning) {
            log.log { "call stopScan() but is not Scanning." }
            return
        }
        log.log { "stop LE SCAN." }
        //先更改状态, 因为没有停止的系统回调
        _isScanning = false
        scanner.stopScan(callback)

        scanListener?.onDeviceScanComplete()
    }

    private fun ScanSettings.fmt(): String {
        val scamMode = when (scanMode) {
            ScanSettings.SCAN_MODE_BALANCED -> "BALANCED"
            ScanSettings.SCAN_MODE_LOW_LATENCY -> "LOW_LATENCY"
            ScanSettings.SCAN_MODE_LOW_POWER -> "LOW_POWER"
            ScanSettings.SCAN_MODE_OPPORTUNISTIC -> "OPPORTUNISTIC"
            else -> scanMode.toString()
        }
        val callbackType = when (callbackType) {
            ScanSettings.CALLBACK_TYPE_ALL_MATCHES -> "ALL_MATCHES"
            ScanSettings.CALLBACK_TYPE_FIRST_MATCH -> "FIRST_MATCH"
            ScanSettings.CALLBACK_TYPE_MATCH_LOST -> "MATCH_LOST"
            else -> callbackType.toString()
        }
        val matchMode = catch {
            val method = ScanSettings::class.java.getDeclaredField("mMatchMode")
            method.isAccessible = true
            when (val m = method.get(this)) {
                ScanSettings.MATCH_MODE_STICKY -> "STICKY"
                ScanSettings.MATCH_MODE_AGGRESSIVE -> "AGGRESSIVE"
                else -> m?.toString()
            }
        }
        return "ModeScan: $scamMode,${matchMode?.let { " ModeMatch: $matchMode," } ?: ""} TypeCallback: $callbackType, TypeScanResult: $scanResultType, delayMillis: $reportDelayMillis, phy: $phy"
    }
}

/**
 * CLASSIC扫描方法实现
 * 处理了相关状态, 并统一回调为OnDeviceScanListener
 */
class CLASSICScanner(private val env: IBluetoothEnv) : ScannerFun, BluetoothHelperFun {

    private val lock = Mutex()
    private var _isScanning = false
        get() = lock.lockWith(coroutineContext) { field }
        set(value) = lock.lockWith(coroutineContext) {
            val old = field
            field = value
            log.log { "CLASSICS SCAN STATE change: $old -> $value." }
            if (value) {
                scanListener?.onDeviceScanStart()
            } else {
                scanListener?.onDeviceScanComplete()
            }
        }
    override val isScanning: Boolean
        get() = _isScanning

    private var receiver: BLUEReceiver? = null
    private var scanListener: OnDeviceScanListener? = null
    private var filter: OnDeviceFilter? = null

    private val callback = object : OnDeviceScanListener {
        override fun onDeviceScanStart() {
            super.onDeviceScanStart()
            launch {
                log.log { "onDeviceScanStart()." }
                if (_isScanning) return@launch
                _isScanning = true
            }
        }

        override fun onDeviceScanned(dev: BluetoothScanDev) {
            launch {
                // 手动停止接收后即不再接收结果
                if (!_isScanning) return@launch
                //过滤
                val isFilter = filter?.isDeviceNeedFilter(dev) ?: false
                log.log { "SCANNED: ${if (isFilter) "$dev Filtered" else "$dev"}." }
                if (!isFilter) {
                    scanListener?.onDeviceScanned(dev)
                }
            }
        }

        //只会在被动结束时回调, 手动结束时不回调
        override fun onDeviceScanComplete() {
            super.onDeviceScanComplete()
            launch {
                log.log { "onDeviceScanComplete()." }
                if (!_isScanning) return@launch
                _isScanning = false

                unregister()
            }
        }
    }

    override fun startScan(filter: OnDeviceFilter, listener: OnDeviceScanListener?) {
        if (receiver != null) {
            log.log { "call startScan() but receiver is not null." }
            return
        }
        if (_isScanning) {
            log.log { "call startScan() but is Scanning." }
            return
        }
        receiver = BLUEReceiver(env.context, callback)
        try {
            runBlocking(Dispatchers.Main) {
                receiver!!.register()
                log.log { "REGISTER classic bluetooth receiver." }
            }
        } catch (e: Exception) {
            log.log { "fail to REGISTER classic bluetooth receiver, do not start scan." }
            return
        }
        this.scanListener = listener
        this.filter = filter
        log.log { "start CLASSIC SCAN." }

        env.adapter?.startDiscovery()
    }

    override fun stopScan() {
        if (!_isScanning) {
            log.log { "call stopScan() but is not Scanning." }
            return
        }
        log.log { "stop CLASSIC SCAN." }
        //虽然有系统回调, 但与LEScanner保持一致
        _isScanning = false
        env.adapter?.cancelDiscovery()

        unregister()
    }

    private fun unregister() {
        try {
            runBlocking(Dispatchers.Main) {
                receiver?.unregister()
                log.log { "UNREGISTER classic bluetooth receiver." }
            }
        } catch (e: Exception) {
            log.log { "fail to UNREGISTER classic bluetooth receiver." }
        }
        receiver = null
    }

    class BLUEReceiver(
        context: Context,
        private val listener: OnDeviceScanListener?,
    ) : ReceiverHelper<OnReceive<BluetoothDev>>(
        context, arrayOf(
            BluetoothDevice.ACTION_FOUND,
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
            BluetoothAdapter.ACTION_DISCOVERY_STARTED
        )
    ) {

        private val log = Logger("BlueReceiver", style = LogStyle.THREAD)

        override fun handleAction(context: Context, action: String, intent: Intent) {

            val actionStr = action.replace("android.bluetooth.device.action.", "")
                .replace("android.bluetooth.adapter.action.", "")

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            ?: return
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, 0).toInt()
                    val dev = BluetoothScanDev(device, BluetoothType.CLASSIC, rssi)
                    log.log { "broadcast receive action: $actionStr(${dev.mac})." }
                    listener?.onDeviceScanned(dev)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    log.log { "broadcast receive action: $actionStr." }
                    listener?.onDeviceScanComplete()
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    log.log { "broadcast receive action: $actionStr." }
                    listener?.onDeviceScanStart()
                }
            }
        }
    }
}