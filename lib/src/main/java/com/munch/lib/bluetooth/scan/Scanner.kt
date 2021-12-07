package com.munch.lib.bluetooth.scan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import androidx.annotation.RequiresPermission
import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.BluetoothType

/**
 * Create by munch1182 on 2021/12/4 17:20.
 */
class Scanner(private val context: Context, private val handler: Handler) : IScanner {

    private val log = BluetoothHelper.logHelper

    private var type: BluetoothType? = null
    private var parameter: ScanParameter? = null
    private var currentScanner: IScanner? = null
    private var listener: OnScannerListener? = null
    private var filterHelper: DeviceFilterHelper? = null
    private val scanLock = Object()
    private var isScanning = false
        get() = synchronized(scanLock) { field }
        set(value) = synchronized(scanLock) { field = value }


    @SuppressLint("MissingPermission")
    private val timeout2Stop = {
        if (isScanning) {
            log.withEnable { "call scan stop() because of timeout(${getParameter().timeout} ms)." }
            stop()
        }
    }

    private val callback = object : OnScannerListener {

        override fun onScanStart() {
            super.onScanStart()
            isScanning = true
            log.withEnable { "scan: onScanStart." }
            handler.post { listener?.onScanStart() }
        }

        override fun onDeviceScanned(dev: BluetoothDev) {
            handler.post {
                val l = listener ?: return@post
                if (filterHelper == null) {
                    l.onDeviceScanned(dev)
                } else {
                    filterHelper?.filterScannedDev(dev)?.let { l.onDeviceScanned(it) }
                }
            }
        }

        override fun onBatchDeviceScanned(devs: Array<BluetoothDev>) {
            handler.post {
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
            isScanning = false
            log.withEnable { "scan: onScanComplete." }
            handler.post {
                handler.removeCallbacks(timeout2Stop)
                listener?.onScanComplete()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onScanFail() {
            super.onScanFail()
            isScanning = true
            log.withEnable { "scan fail, call scan stop()." }

            handler.removeCallbacks(timeout2Stop)
            handler.post { listener?.onScanFail() }

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
            BluetoothType.BLE -> BleScanner(context, p.toType(), callback)
            BluetoothType.CLASSIC -> ClassicScanner(context, callback)
        }

        filterHelper = DeviceFilterHelper(p)

        handler.postDelayed(timeout2Stop, p.timeout)

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