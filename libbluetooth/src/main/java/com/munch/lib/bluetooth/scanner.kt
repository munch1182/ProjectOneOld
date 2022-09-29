package com.munch.lib.bluetooth

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.munch.lib.android.helper.ARSHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.munch.lib.android.extend.suspendCancellableCoroutine
import kotlin.Exception

/**
 * Create by munch1182 on 2022/9/29 16:20.
 */
abstract class BaseBluetoothScanner : ARSHelper<OnBluetoothDevScannedListener?>(),
    IBluetoothScanner, IBluetoothManager by BluetoothEnv {

    protected open val log = BluetoothHelper.log

    private val lock4Scanning = Mutex()
    private val lock4FindDev = Mutex()

    protected open val from = From()

    /**
     * 是否正在扫描的真实状态
     */
    protected open var scanning: Boolean = false
        get() = runBlocking { lock4Scanning.withLock { field } }
        set(value) = runBlocking {
            lock4Scanning.withLock {
                val last = field
                field = value
                log.log("Scan state change: $last -> $field")
            }
        }

    /**
     * 为了寻找设备的扫描的回调
     */
    protected open var scanCallback4FindDev: OnBluetoothDevScannedListener? = null
        get() = runBlocking { lock4FindDev.withLock { field } }
        set(value) = runBlocking { lock4FindDev.withLock { field = value } }

    /**
     * 当前蓝牙扫描器是否正在扫描中
     */
    override val isScanning: Boolean
        get() = scanning && from.isFromCALL() // 当正在扫描但是是因为寻找设备时, 此方法返回false

    /**
     * 一个设备后调后, 延时[delayTime]后再发送下一个设备, 如果为0则不延时
     */
    protected open var delayTime = 300L

    /**
     * 扫描设备时的过滤器
     */
    protected open var filter: OnBluetoothDevFilter? = null

    /**
     * 设置一个延时, 当发送一个设备后, 延时[time]之后再发送另一个, 如果为0则不延时
     */
    fun setDelayTime(time: Long): BaseBluetoothScanner {
        delayTime = time
        return this
    }

    override fun setScanFilter(filter: OnBluetoothDevFilter?): IBluetoothScanner {
        this.filter = filter
        return this
    }

    override fun addScanListener(l: OnBluetoothDevScannedListener?) {
        add(l)
    }

    override fun removeScanListener(l: OnBluetoothDevScannedListener?) {
        remove(l)
    }

    override suspend fun find(mac: String, timeout: Long): IBluetoothDev? {
        try {
            suspendCancellableCoroutine<IBluetoothDev>(timeout) {
                scanCallback4FindDev = OnBluetoothDevScannedListener {

                }
            }

        } catch (e: Exception) {

        } finally {

        }
        return null
    }

    //<editor-fold desc="处理find引起的扫描中的问题">
    class From {
        companion object {
            const val VALUE_FROM_CALL = 1
            const val VALUE_FROM_FIND = 2
            const val VALUE_FROM_BOTH = VALUE_FROM_CALL + VALUE_FROM_FIND
        }

        private val lock = Mutex()
        private var value = 0
            get() = runBlocking { lock.withLock { field } }
            set(value) = runBlocking { lock.withLock { field = value } }

        fun fromFIND() {
            value += VALUE_FROM_FIND
        }

        fun fromCALL() {
            value += VALUE_FROM_CALL
        }

        fun completeFIND() {
            value -= VALUE_FROM_FIND
        }

        fun completeCALL() {
            value -= VALUE_FROM_CALL
        }

        fun isFromFIND() = value == VALUE_FROM_FIND || value == VALUE_FROM_BOTH
        fun isFromCALL() = value == VALUE_FROM_CALL || value == VALUE_FROM_BOTH
    }

    //</editor-fold>
}

/**
 *  LE蓝牙扫描
 */
object BluetoothLeScanner : BaseBluetoothScanner(), CoroutineScope by BluetoothHelper {

    private var channel: Channel<IBluetoothDev>? = null // 使用channel平缓发送发现设备

    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
            launch { channel?.send(BluetoothScanDev(result)) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            log.log("onScanFailed: ${errorCode.fmt()}.")
            stopScan()
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            launch { results?.forEach { channel?.send(BluetoothScanDev(it)) } }
        }

        private fun Int.fmt(): String {
            return when (this) {
                SCAN_FAILED_ALREADY_STARTED -> "ALREADY_STARTED"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "APPLICATION_REGISTRATION_FAILED"
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "FEATURE_UNSUPPORTED "
                SCAN_FAILED_INTERNAL_ERROR -> "INTERNAL_ERROR"
                else -> toString()
            }
        }
    }
    private val defSet = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .build()
    private var set: ScanSettings? = null
        get() = field ?: defSet


    fun setScanSetting(set: ScanSettings): BluetoothLeScanner {
        this.set = set
        return this
    }

    override fun startScan() {
        if (isScanning) {
            return
        }
        from.fromCALL()
        realStartScan()
    }

    override fun stopScan() {
        if (!isScanning) {
            return
        }
        from.completeCALL()
        if (!from.isFromFIND()) { // 如果此时还有find任务未完成, 实际不需要停止扫描
            realStopScan()
        }
    }

    private fun realStartScan() {
        val set = ScanSettings.Builder().build()
        val newChannel = Channel<IBluetoothDev>()
        channel = newChannel
        adapter?.bluetoothLeScanner?.startScan(null, set, callback)

        launch(Dispatchers.Default) {
            for (dev in newChannel) {
                if (delayTime > 0) delay(delayTime)
                update { it?.onDevScanned(dev) }
            }
        }
    }

    private fun realStopScan() {
        adapter?.bluetoothLeScanner?.stopScan(callback)
        channel?.close()
        channel = null
    }
}

/**
 * 经典蓝牙扫描
 */
object BluetoothClassicScanner : BaseBluetoothScanner() {
    override fun startScan() {
    }

    override fun stopScan() {
    }
}