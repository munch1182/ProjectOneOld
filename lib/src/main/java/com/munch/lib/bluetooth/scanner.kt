package com.munch.lib.bluetooth

import android.bluetooth.le.ScanSettings


/**
 * Create by munch1182 on 2021/12/3 16:57.
 */
interface IScanner {

    fun start() {}

    fun stop() {}
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
     * 要寻找的目标设备地址
     */
    var target: List<String>? = null


    abstract fun default(): ScanParameter

    override fun toString(): String {
        return "ScanParameter(timeout=$timeout, justFirst=$justFirst), target=${target?.joinToString()}"
    }

    object BleScanParameter : ScanParameter() {

        /**
         * 设置回复时间, 大于0将启用批量模式(如果设备支持)
         */
        var reportDelay = 0L
        var set: ScanSettings? = null

        override fun default() = this

        override fun toString(): String {
            return "BleScanParameter(timeout=$timeout, justFirst=$justFirst), target=${target?.joinToString()}, " +
                    "reportDelay=$reportDelay, set=$set"
        }
    }

    object ClassicScanParameter : ScanParameter() {

        override fun default() = this
    }


}

class Scanner(private val type: BluetoothType) : IScanner {

    private var parameter: ScanParameter? = null
    private var currentScanner: IScanner? = null
    private val log = BluetoothHelper.logHelper

    fun build(parameter: ScanParameter? = null): Scanner {
        this.parameter = parameter
        return this
    }

    override fun start() {
        super.start()
        if (currentScanner != null) {
            log.withEnable { "must stop scan first" }
            return
        }
        val p = parameter ?: getDefault()
        currentScanner = when (type) {
            BluetoothType.BLE -> BleScanner(p)
            BluetoothType.CLASSIC -> ClassicScanner(p)
        }
        currentScanner?.start()
        log.withEnable { "start scan, parameter = $p" }
        BluetoothHelper.instance.handler.postDelayed({
            stop()
            log.withEnable { "call scan stop() because of timeout(${p.timeout} ms)." }
        }, p.timeout)
    }

    override fun stop() {
        super.stop()
        log.withEnable { "stop scan" }
        currentScanner?.stop()
        currentScanner = null
    }

    private fun getDefault(): ScanParameter {
        return when (type) {
            BluetoothType.BLE -> ScanParameter.BleScanParameter.default()
            BluetoothType.CLASSIC -> ScanParameter.ClassicScanParameter.default()
        }
    }
}

class BleScanner(p: ScanParameter) : IScanner {

    override fun start() {
        super.start()
    }

    override fun stop() {
        super.stop()
    }
}

class ClassicScanner(p: ScanParameter) : IScanner {

    override fun start() {
        super.start()
    }

    override fun stop() {
        super.stop()
    }
}