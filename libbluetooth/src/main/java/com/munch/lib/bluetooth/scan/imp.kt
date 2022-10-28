package com.munch.lib.bluetooth.scan

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.munch.lib.android.extend.lazy
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.android.helper.ILifecycle
import com.munch.lib.android.helper.MutableLifecycle
import com.munch.lib.bluetooth.dev.BluetoothScanDev
import com.munch.lib.bluetooth.env.BluetoothEnv
import com.munch.lib.bluetooth.env.IBluetoothManager
import com.munch.lib.bluetooth.env.IBluetoothState
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.BluetoothOnOff
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import com.munch.lib.bluetooth.helper.onOff2Notify
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * Create by munch1182 on 2022/10/26 16:51.
 */

/**
 * imp的基类, 处理状态变更, 其已经关联[IBluetoothState], 当蓝牙关闭时自动关闭扫描
 */
internal abstract class BluetoothStateScanner :
    IBluetoothScanner,
    ARSHelper<OnBluetoothDevScannedListener>(),
    IBluetoothState by BluetoothEnv,
    IBluetoothHelperEnv by BluetoothHelperEnv,
    ILifecycle by MutableLifecycle() {

    private val btOnOff: BluetoothOnOff by lazy {
        {
            if (!it && scanning) { // DEV SCANNER因为顺序原因, 回调此时已经被关闭扫描
                log("stop scan as OFF.")
                stopScan()
            }
        }
    }
    private val onOffNotify by lazy { btOnOff.onOff2Notify() }

    private val scanningLock = Mutex()
    protected open var scanning = false
        get() = runBlocking { scanningLock.withLock { field } }
        set(value) = runBlocking {
            if (field == value) return@runBlocking
            val last = field
            scanningLock.withLock { field = value }
            logState(last, field)

            if (value) {
                addStateChangeListener(onOffNotify)
            } else {
                removeStateChangeListener(onOffNotify)
            }

            // 不能在锁内, 因为可以会被外部调用scanning而导致死锁
            update {
                if (it is OnBluetoothDevScanLifecycleListener) {
                    if (value) it.onScanStart() else it.onScanStop()
                }
            }
        }

    protected open fun logState(last: Boolean, curr: Boolean) {
        log("Scan state: $last -> $curr.")
    }

    override fun log(content: String) {
        log.log("DEV SCANNER: $content")
    }

    override val isScanning: Boolean
        get() = this.scanning

}

/**
 * 表示设备的扫描状态, 其类唯一
 *
 * 如果有多个扫描活动, 此库的扫描状态从第一个开始的扫描活动到最后一个扫描活动停止时为true
 */
internal object BluetoothLeDevScanner :
    BluetoothStateScanner(), IBluetoothDevScanner,
    IBluetoothManager by BluetoothEnv {

    /**
     * 用于标记当前进行的扫描活动, 新有一个扫描活动加1, 一个扫描活动停止时减1, 为0时表示无活动, 则停止扫描
     */
    private var startCount = AtomicInteger(0)

    //<editor-fold desc="callback">
    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return
            sendDev(result)
        }

        private fun sendDev(result: ScanResult) {
            launch { update { it.onBluetoothDevScanned(BluetoothScanDev(result)) } }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            log("DEV SCANNER: onScanFailed: ${errorCode.fmt()}.")
            stopScan()
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { sendDev(it) }
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
    //</editor-fold>

    private val defSet = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .build()

    private var set: ScanSettings? = null
        get() = field ?: defSet

    fun setScanSetting(set: ScanSettings): BluetoothLeDevScanner {
        this.set = set
        return this
    }

    override fun startScan(timeout: Long) {
        startCount.incrementAndGet() // 增加一次扫描计数
        if (scanning) {
            //log("call start scan but scanning.")
            return
        }

        // 更改状态
        this.scanning = true

        log("start LE scan.")

        adapter?.bluetoothLeScanner?.startScan(null, set, callback)
    }

    override fun stopScan() {
        if (!this.scanning) return

        val count = startCount.decrementAndGet() // 减少一次扫描计数
        if (count > 0) { // 只有为0时即没有活动的扫描器, 则可以关闭
            log("call stop LE scan, left count: $count.")
            return
        }

        log("stop LE scan.")
        adapter?.bluetoothLeScanner?.stopScan(callback)

        this.scanning = false
    }

    override fun addScanListener(l: OnBluetoothDevScannedListener) {
        add(l)
    }

    override fun removeScanListener(l: OnBluetoothDevScannedListener) {
        remove(l)
    }

    override fun log(content: String) {
        log.log("DEV SCANNER: $content")
    }
}

internal object BluetoothClassicScanner : BluetoothStateScanner(), IBluetoothDevScanner {

    override fun startScan(timeout: Long) {
    }

    override fun stopScan() {
    }

    override fun addScanListener(l: OnBluetoothDevScannedListener) {
        add(l)
    }

    override fun removeScanListener(l: OnBluetoothDevScannedListener) {
        remove(l)
    }
}
