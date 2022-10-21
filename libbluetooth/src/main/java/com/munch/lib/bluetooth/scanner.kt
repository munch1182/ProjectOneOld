package com.munch.lib.bluetooth

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.munch.lib.android.extend.to
import com.munch.lib.android.helper.ARSHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create by munch1182 on 2022/9/29 16:20.
 */
abstract class BaseBluetoothScanner : ARSHelper<OnBluetoothDevScannedListener?>(),
    IBluetoothScanner, IBluetoothManager by BluetoothEnv {

    protected open val log = BluetoothHelper.log

    private val lock4Scanning = Mutex()

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
                update {
                    if (it is OnBluetoothDevScanListener) {
                        if (value) {
                            it.onScanStart()
                        } else {
                            it.onScanStop()
                        }
                    }
                }
            }
        }


    /**
     * 当前蓝牙扫描器是否正在扫描中
     */
    override val isScanning: Boolean
        get() = scanning

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

    override fun startScan(timeout: Long) {
        if (isScanning) {
            log.log("call start scan but scanning.")
            return
        }

        // 更改状态
        scanning = true

        val newChannel = Channel<IBluetoothDev>()
        channel = newChannel

        log.log("start LE scan.")
        adapter?.bluetoothLeScanner?.startScan(null, set, callback)

        launch(Dispatchers.Default) {
            withTimeout(timeout) {
                for (dev in newChannel) {
                    if (filter?.isDevNeedFiltered(dev) == true) {
                        continue
                    }
                    if (delayTime > 0) delay(delayTime)
                    log.log("receive dev: $dev.")
                    update { it?.onDevScanned(dev) }
                }
            }
            stopScan()
        }
    }

    override fun stopScan() {
        if (!isScanning) {
            return
        }
        channel?.close()
        channel = null
        adapter?.bluetoothLeScanner?.stopScan(callback)
    }
}

/**
 * 经典蓝牙扫描
 */
object BluetoothClassicScanner : BaseBluetoothScanner() {
    override fun startScan(timeout: Long) {
    }

    override fun stopScan() {
    }
}

/**
 * 给[BluetoothHelper]暴露的Scanner相关方法
 */
interface IBluetoothHelperScanner : IBluetoothScanner {

    fun config(config: Builder.() -> Unit): IBluetoothHelperScanner

    class Builder {
        internal var type: BluetoothType = BluetoothType.LE
        internal var filter: OnBluetoothDevFilter? = null
        internal var delayTime = 0L

        fun type(type: BluetoothType): Builder {
            this.type = type
            return this
        }

        fun filter(vararg filter: OnBluetoothDevFilter): Builder {
            this.filter = if (filter.size == 1) {
                filter.first()
            } else if (filter.size > 1) {
                BluetoothDevFilterContainer(*filter)
            } else {
                null
            }
            return this
        }

        fun setDelayTime(time: Long): Builder {
            this.delayTime = time
            return this
        }

        fun build(): Builder {
            return this
        }
    }
}

/**
 * 用于切换和管理两种类型的Scanner
 */
object BluetoothHelperScanner : IBluetoothHelperScanner {

    private val config = IBluetoothHelperScanner.Builder()
        .type(BluetoothType.LE)
        .filter(BluetoothDevNoNameFilter(), BluetoothDevFirstFilter())
    private var scanner: IBluetoothScanner = getScanner()

    private fun getScanner(): IBluetoothScanner {
        return when (config.type) {
            BluetoothType.CLASSIC -> BluetoothClassicScanner
            BluetoothType.LE -> BluetoothLeScanner
            else -> throw IllegalArgumentException()
        }
    }

    override fun config(config: IBluetoothHelperScanner.Builder.() -> Unit): IBluetoothHelperScanner {
        config.invoke(this.config)
        scanner = getScanner()
        this.config.filter?.let { setScanFilter(it) }
        if (scanner is BaseBluetoothScanner) {
            scanner.to<BaseBluetoothScanner>().setDelayTime(this.config.delayTime)
        }
        return this
    }

    override val isScanning: Boolean
        get() = scanner.isScanning

    override fun setScanFilter(filter: OnBluetoothDevFilter?): IBluetoothScanner {
        scanner.setScanFilter(filter)
        return this
    }

    override fun startScan(timeout: Long) {
        scanner.startScan(timeout)
    }

    override fun stopScan() {
        scanner.stopScan()
    }

    override fun addScanListener(l: OnBluetoothDevScannedListener?) {
        scanner.addScanListener(l)
    }

    override fun removeScanListener(l: OnBluetoothDevScannedListener?) {
        scanner.removeScanListener(l)
    }

}