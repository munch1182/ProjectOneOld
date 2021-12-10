package com.munch.lib.bluetooth.scan

import android.annotation.SuppressLint
import android.bluetooth.le.*
import android.os.Build
import androidx.annotation.RequiresPermission
import com.munch.lib.base.Resettable
import com.munch.lib.bluetooth.BluetoothDev

/**
 * Create by munch1182 on 2021/12/3 16:57.
 */
interface IScanner {

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun start() {
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun stop() {
    }
}

interface OnScannerListener {

    fun onScanStart() {}

    /**
     * 未设置批量处理的返回
     */
    fun onDeviceScanned(dev: BluetoothDev) {}

    /**
     * 设置批量处理的返回
     */
    fun onBatchDeviceScanned(devs: Array<BluetoothDev>) {}

    fun onScanComplete() {}

    fun onScanFail() {}
}

sealed class ScanParameter {

    /**
     * 超时时间
     */
    var timeout = 25 * 1000L

    /**
     * 设备是否只返回一次，即是否重复回调相同的设备
     *
     * 相同的设备的多次回调信息(比如信号强度)会更新
     */
    var justFirst: Boolean = true

    /**
     * 不返回没有名称的蓝牙地址，除非这个地址在[target]中
     */
    var ignoreNoName: Boolean = true

    /**
     * 要寻找的目标设备
     */
    var target: List<Target>? = null

    abstract fun default(): ScanParameter

    inline fun <reified T : ScanParameter> toType(): T {
        return if (T::class.java == BleScanParameter::class.java && this !is BleScanParameter) {
            BleScanParameter().apply {
                timeout = this@ScanParameter.timeout
                target = this@ScanParameter.target
                justFirst = this@ScanParameter.justFirst
                ignoreNoName = this@ScanParameter.ignoreNoName
            } as T
        } else {
            this as T
        }
    }

    override fun toString(): String {
        return "ScanParameter(timeout=$timeout, justFirst=$justFirst), ignoreNoName=$ignoreNoName, target=${target?.joinToString()}"
    }

    data class Target(
        val name: String? = null,
        val mac: String? = null,
        //是否需要完全匹配
        val exact: Boolean = true
    ) {

        /**
         * 如果target两个参数都有设置，则需要两个参数都匹配
         * 如果只需要匹配一个参数，则将另一个参数设为null
         */
        fun match(n: String?, m: String?): Boolean {
            if (invalid) {
                return false
            }
            return if (exact) {
                (name?.let { it == n } ?: true) && (mac?.let { it == m } ?: true)
            } else {
                judgeNotExactMatch(n, name) && judgeNotExactMatch(m, mac)
            }
        }

        private fun judgeNotExactMatch(s1: String?, s2: String?) =
            if (!s1.isNullOrEmpty() && !s2.isNullOrEmpty()) s1.contains(s2) else true

        val invalid: Boolean
            get() = name.isNullOrEmpty() && mac.isNullOrEmpty()
    }

    class BleScanParameter : ScanParameter() {

        companion object {
            fun defaultBleScanSetBuilder(): ScanSettings.Builder {
                return ScanSettings.Builder()
                    //有过滤条件时过滤，返回符合过滤条件的蓝牙广播；无过滤条件时，返回全部蓝牙广播
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    //粘性模式，在通过硬件报告之前，需要更高的信号强度和目击阈值
                    .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                    //高功耗模式，如果扫描时app不再前台，则此设置无效，会默认使用ScanSettings.SCAN_MODE_LOW_POWER 低功耗模式
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            }
        }

        var set: ScanSettings? = null

        val bleScanSet: ScanSettings
            get() = set ?: defaultBleScanSetBuilder().build()

        override fun default() = this

        override fun toString(): String {
            return "BleScanParameter(timeout=$timeout, justFirst=$justFirst, target=${target?.joinToString()}, ignoreNoName=$ignoreNoName, set=${bleScanSet.toStr()})"
        }

        private fun ScanSettings.toStr(): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                "(callbackType: $callbackType, scanMode: $scanMode, reportDelayMillis: $reportDelayMillis, scanResultType: $scanResultType, legacy: $legacy, phy: $phy)"
            } else {
                "(callbackType: $callbackType, scanMode: $scanMode, reportDelayMillis: $reportDelayMillis, scanResultType: $scanResultType)"
            }
        }
    }

    class ClassicScanParameter : ScanParameter() {

        override fun default() = this
    }
}

class DeviceFilterHelper(private val p: ScanParameter) : Resettable {

    private val devs = mutableSetOf<BluetoothDev>()

    fun filterScannedDev(dev: BluetoothDev): BluetoothDev? {
        if (p.justFirst) {
            if (devs.contains(dev)) {
                return null
            }
        }
        devs.add(dev)
        p.target?.let { l ->
            l.forEach {
                if (it.match(dev.name, dev.mac)) {
                    return dev
                }
            }
            return null
        }
        if (p.ignoreNoName && dev.name.isNullOrEmpty()) {
            return null
        }
        return dev
    }

    fun filterScannedDevs(devs: Array<BluetoothDev>): Array<BluetoothDev>? {
        return devs.filter { filterScannedDev(it) != null }.takeIf { it.isNotEmpty() }
            ?.toTypedArray()
    }

    override fun reset() {
        devs.clear()
    }
}