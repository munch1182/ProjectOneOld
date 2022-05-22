package com.munch.lib.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.munch.lib.RepeatStrategy
import com.munch.lib.log.Logger
import kotlinx.coroutines.*
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2022/5/18 17:00.
 */
interface IBluetoothFilter {

    /**
     * 过滤设备是否符合要求
     *
     * @param dev 当前扫描到的设备
     *
     * @return 如果设备符合要求，则返回设备，否则，返回null
     */
    fun onFilter(dev: BluetoothDev): BluetoothDev?

    /**
     * 是否已经获取到需要的设备
     *
     * @return 如果不需要再寻找设备，则返回true，否则，返回false
     */
    fun enough(dev: BluetoothDev, devMap: LinkedHashMap<String, BluetoothDev>): Boolean {
        return false
    }
}

class ScanFilter : IBluetoothFilter {

    /**
     * 用于寻找的特定设备地址
     *
     * 如果该值不为null
     * 1. 如果该值为标志的mac地址，则扫描将在扫到该设备后立即停止扫描并返回设备对象
     * 2. 否则，则扫描所有带有该字符的蓝牙设备
     */
    var mac: String? = null
        set(value) {
            field = value
            isMatchMac = BluetoothAdapter.checkBluetoothAddress(value)
        }

    /**
     * 用于寻找名称包含该值的设备
     */
    var name: String? = null

    /**
     * 是否忽略没有名称的蓝牙设备
     */
    var isIgnoreNoName = true

    /**
     * 只返回信号小于该值的蓝牙设备
     */
    var rssi = 0

    /**
     * 是否mac是一个完整的mac地址
     * 需要同mac更改时同步更改，可以避免重复检查
     */
    private var isMatchMac: Boolean = false

    override fun onFilter(dev: BluetoothDev): BluetoothDev? {
        val address = mac
        if (address != null) {
            if (isMatchMac) {
                if (dev.mac != address) {
                    return null
                }
            } else {
                if (!dev.mac.contains(address)
                //多余判断，应该让输入的mac保持格式
                /*&& !dev.mac.replace(":", "").contains(address)*/
                ) {
                    return null
                }
            }
        }

        val devName = dev.name
        if (devName == null && isIgnoreNoName) {
            return null
        }

        val targetName = name
        if (targetName != null) {
            if (devName == null) {
                return null
            } else if (!devName.contains(targetName)) {
                return null
            }
        }
        //忽略dev.rssi为0的设备，因为那代表着未获取到信号
        if (dev.rssi != 0 && rssi != 0 && rssi.absoluteValue < dev.rssi.absoluteValue) {
            return null
        }
        return dev
    }

    override fun enough(dev: BluetoothDev, devMap: LinkedHashMap<String, BluetoothDev>): Boolean {
        return mac?.let { if (isMatchMac) dev.mac == it else false } ?: false
    }

    override fun toString(): String {
        val sb = StringBuilder()
        var index = 0
        mac?.let {
            index++
            sb.append("mac=$it")
        }
        name?.let {
            if (index > 0) {
                sb.append(", ")
            }
            index++
            sb.append("name=$it")
        }
        if (isIgnoreNoName) {
            if (index > 0) {
                sb.append(", ")
            }
            index++
            sb.append("isIgnoreNoName=$isIgnoreNoName")
        }
        if (rssi != 0) {
            if (index > 0) {
                sb.append(", ")
            }
            index++
            sb.append("rssi=$rssi")
        }
        return if (index == 0) "Filter(None)" else "Filter($sb)"

    }

}

class ScanTarget {
    companion object {
        const val TIMEOUT = 10000L
    }

    /**
     * 扫描的超时时间，ms
     */
    var timeout = TIMEOUT

    /**
     * 扫描的过滤，被过滤的设备将不会回调
     */
    var filter: IBluetoothFilter = ScanFilter()

    /**
     * 扫描时，如果正在扫描中，执行的策略
     */
    var repeatStrategy: RepeatStrategy = RepeatStrategy.Ignore

    /**
     * 扫描设置，Ble限定
     */
    var scanSetting: ScanSettings = ScanSettings.Builder()
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    /**
     * 用以辨别此次扫描是否是由用户发起，否则是由程序发起
     *
     * 可能在程序上存在程序正在使用扫描程序，但是用户又发起了扫描的情形
     * 但是目前从逻辑上看不可能出现这种情形
     */
    internal var isFromUser = true

    override fun toString(): String {
        return "ScanTarget(timeout=${timeout}ms, filter=$filter, repeatStrategy=$repeatStrategy, scanSetting=$scanSetting)"
    }
}

inline fun ScanTarget.ScanFilter(init: ScanFilter.() -> Unit) {
    filter = ScanFilter().apply(init)
}

inline fun ScanTarget(init: ScanTarget.() -> Unit) = ScanTarget().apply(init)

@SuppressLint("MissingPermission")
internal class BleScanner(
    private val bm: IBluetoothManager? = null,
    private var handler: Handler? = null
) : Scanner {

    companion object {
        private const val WHAT_TIMEOUT_STOP = 519
    }

    private val log: Logger
        get() = BluetoothHelper.log

    /**
     * ble scanner对象
     */
    private val scanner: BluetoothLeScanner?
        get() = bm?.adapter?.bluetoothLeScanner.also {
            if (it == null) {
                log.log { "scanner is null. " }
            }
        }

    /**
     * 用于存储扫描到的设备，可用于过滤重复扫描
     */
    private val devMap = LinkedHashMap<String, BluetoothDev>()

    /**
     * 扫描系统回调
     */
    private val callback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (!_isScanning) {
                return
            }
            val dev = result?.let { BluetoothDev.from(result) } ?: return
            //log.log { "onScanResult ${dev.mac}." }
            //结果在子线程中处理
            handler?.post {
                val target = scanTarget ?: return@post
                val filter = target.filter
                if (filter.onFilter(dev) == null) {
                    return@post
                }

                devMap[dev.mac] = dev
                if (target.isFromUser) {
                    log.log { "onScanResult filter:${dev.name}(${dev.mac})." }
                    //全局扫描回调不会在程序中被注册
                    registeredScanListener?.onScanned(dev, devMap)
                }
                scanListener?.onScanned(dev, devMap)
                if (filter.enough(dev, devMap)) {
                    stopBy()
                }
            }
        }
    }

    /**
     * 单次扫描回调
     */
    private var scanListener: ScanListener? = null

    /**
     * 注册的扫描回调
     */
    private var registeredScanListener: ScanListener? = null

    /**
     * 此处扫描的设置
     */
    private var scanTarget: ScanTarget? = null

    /**
     * 超时停止扫描机制
     */
    private val stopRunnable = {
        log.log { "scanner stop runnable call(${scanTarget?.timeout}ms). isScanning:$_isScanning." }
        if (_isScanning) {
            stopBy()
        }
    }

    /**
     * 当前是否正在进行扫描活动
     */
    private var _isScanning = false
        get() = runBlocking { synchronized(BleScanner::class.java) { field } }
        set(value) = runBlocking {
            synchronized(BleScanner::class.java) {
                //dev
                if (field == value) {
                    throw IllegalStateException("scan state repeat: $value")
                }
                field = value
                _isScanningData.postValue(field)
                log.log { "curr isScanning: $_isScanning." }
                if (field) {
                    scanListener?.onStart()
                } else {
                    scanListener?.onComplete()
                }
            }
        }

    private val _isScanningData = MutableLiveData(_isScanning)

    override val isScanning: LiveData<Boolean> = _isScanningData

    override val isScanningNow: Boolean
        get() = _isScanning

    fun setHandler(handler: Handler?) {
        this.handler = handler
    }

    override fun scan(target: ScanTarget, listener: ScanListener?): Boolean {
        log.log { "scanner scan() call. target = $target" }
        if (scanner == null) {
            log.log { "scanner error: scanner=$scanner." }
            return false
        }
        if (_isScanning) {
            val strategy = target.repeatStrategy
            if (strategy == RepeatStrategy.Ignore) {
                log.log { "isScanning = ${isScanning}, ignore scanner scan()." }
                return true
            }
            if (strategy == RepeatStrategy.Replace) {
                log.log { "isScanning = ${isScanning}, scanner stop now." }
                stopBy()
                return scan(target, listener)
            }
            return false
        }

        clear()
        scanTarget = target
        scanListener = listener

        timeoutStop(target.timeout)

        _isScanning = true

        log.log { "scanner START SCAN." }

        scanner?.startScan(null, target.scanSetting, callback)

        return true
    }

    private fun timeoutStop(timeout: Long) {
        handler?.apply {
            clearTimeout()
            val msg = Message.obtain(this, stopRunnable)
            msg.what = WHAT_TIMEOUT_STOP
            sendMessageDelayed(msg, timeout)
        }
    }

    private fun clearTimeout() {
        handler?.removeMessages(WHAT_TIMEOUT_STOP)
    }

    override fun stop(): Boolean {
        return stopBy(true)
    }

    private fun stopBy(byUser: Boolean = false): Boolean {
        log.log { "scanner stop() call by user($byUser)." }
        if (!_isScanning) {
            log.log { "isScanning = ${isScanning}, ignore scanner stop()." }
            return true
        }
        log.log { "scanner STOP SCAN." }

        scanner?.stopScan(callback)

        _isScanning = false

        clear()

        return true
    }

    private fun clear() {
        clearTimeout()
        scanTarget = null
        scanListener = null
        devMap.clear()
    }

    override fun registerScanListener(t: ScanListener?) {
        registeredScanListener = t
    }

    override fun unregisterScanListener(t: ScanListener?) {
        registeredScanListener = null
    }

}