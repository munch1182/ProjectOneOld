package com.munch.lib.bluetooth.scan

import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.*

/**
 * Create by munch1182 on 2021/12/4 17:20.
 */
internal object Scanner : IScanner {

    private val log = BluetoothHelper.logHelper
    private val instance = BluetoothHelper.instance

    private var type: BluetoothType? = null
    private var parameter: ScanParameter? = null
    private var currentScanner: IScanner? = null
    private var listener: OnScannerListener? = null
    private var filterHelper: DeviceFilterHelper? = null

    @SuppressLint("MissingPermission")
    private val timeout2Stop = {
        if (instance.state.isSCANNING) {
            log.withEnable { "call scan stop() because of timeout(${getParameter().timeout} ms)." }
            stop()
        }
    }

    private val callback = object : OnScannerListener {

        override fun onScanStart() {
            super.onScanStart()
            log.withEnable { "scan: onScanStart." }
            instance.handler.post {
                instance.state.updateSCANNINGState()
                listener?.onScanStart()
            }
        }

        override fun onDeviceScanned(dev: BluetoothDev) {
            instance.handler.post {
                val l = listener ?: return@post
                if (filterHelper == null) {
                    l.onDeviceScanned(dev)
                } else {
                    filterHelper?.filterScannedDev(dev)?.let { l.onDeviceScanned(it) }
                }

            }
        }

        override fun onBatchDeviceScanned(devs: Array<BluetoothDev>) {
            instance.handler.post {
                val l = listener ?: return@post
                if (filterHelper == null) {
                    l.onBatchDeviceScanned(devs)
                } else {
                    filterHelper?.filterScannedDevs(devs)?.let { l.onBatchDeviceScanned(it) }
                }
            }
        }

        override fun onScanComplete() {
            super.onScanComplete()
            log.withEnable { "scan: onScanComplete." }
            instance.handler.post {
                instance.state.updateIDLEState()
                instance.handler.removeCallbacks(timeout2Stop)
                listener?.onScanComplete()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onScanFail() {
            super.onScanFail()

            listener?.onScanFail()
            instance.handler.removeCallbacks(timeout2Stop)

            log.withEnable { "scan fail, call scan stop()." }
            stop()
        }
    }

    fun type(type: BluetoothType): Scanner {
        this.type = type
        return this
    }

    fun build(parameter: ScanParameter? = null): Scanner {
        this.parameter = parameter
        return this
    }

    fun setScanListener(listener: OnScannerListener?): Scanner {
        this.listener = listener
        return this
    }

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    override fun start() {
        super.start()
        if (currentScanner != null) {
            log.withEnable { "must stop scan first." }
            return
        }
        val type = requireNotNull(type) { "must set type first." }

        callback.onScanStart()

        val p = getParameter()
        currentScanner = when (type) {
            BluetoothType.BLE -> BleScanner(p.toType(), callback)
            BluetoothType.CLASSIC -> ClassicScanner(instance.context, callback)
        }

        filterHelper = DeviceFilterHelper(p)

        instance.handler.postDelayed(timeout2Stop, p.timeout)

        log.withEnable { "start scan, parameter = $p." }
        currentScanner?.start()
    }

    private fun getParameter() = parameter ?: getDefault()

    @SuppressLint("InlinedApi")
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    override fun stop() {
        super.stop()
        currentScanner?.let {
            log.withEnable { "stop scan." }
            it.stop()
        }
        currentScanner = null
    }

    private fun getDefault(): ScanParameter {
        return when (requireNotNull(type) { "must set type first." }) {
            BluetoothType.BLE -> ScanParameter.BleScanParameter().default()
            BluetoothType.CLASSIC -> ScanParameter.ClassicScanParameter().default()
        }
    }
}