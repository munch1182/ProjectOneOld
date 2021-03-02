package com.munch.lib.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.munch.lib.helper.ReceiverHelper
import android.bluetooth.le.ScanFilter as Filter

/**
 * Create by munch1182 on 2021/3/2 17:22.
 */
data class ScanFilter(
    val name: String? = null, val mac: String? = null,
    //是否严格匹配，即两个参数完全相同
    val strict: Boolean = true
)

interface BtScanListener {

    fun onStart()

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_PRIVILEGED]
    )
    fun onScan(device: BtDeviceBean)

    fun onEnd(device: MutableList<BtDeviceBean>)
}

@SuppressLint("MissingPermission")
sealed class BtScanner {

    protected var listener: BtScanListener? = null
    protected var res: MutableList<BtDeviceBean> = mutableListOf()

    class ClassicScanner : BtScanner() {

        private val classicBtReceiver by lazy { ClassicBtReceiver(BTHelper.getInstance().context) }
        private var filters: MutableList<ScanFilter> = mutableListOf()

        override fun start(filters: MutableList<ScanFilter>?) {
            super.start(filters)
            this.filters.clear()
            if (filters != null) {
                this.filters.addAll(filters)
            }
            if (listener != null) {
                classicBtReceiver.add(listener!!)
            }
            classicBtReceiver.register()
            BluetoothAdapter.getDefaultAdapter().startDiscovery()
        }

        override fun stop() {
            super.stop()
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
                            t.onScan(BtDeviceBean.from(device, rssi!!.toInt()))
                        } else {
                            kotlin.run out@{
                                filters.forEach {
                                    if (it.strict) {
                                        if (device.name == it.name && device.address == it.mac) {
                                            t.onScan(BtDeviceBean.from(device, rssi!!.toInt()))
                                            return@out
                                        }
                                    } else {
                                        if ((device.name != null && it.name != null
                                                    && device.name.contains(it.name))
                                            || (it.mac != null && device.address.contains(it.mac))
                                        ) {
                                            t.onScan(BtDeviceBean.from(device, rssi!!.toInt()))
                                            return@out
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class BleScanner : BtScanner() {

        private val callBack = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (result != null) {
                    listener?.onScan(BtDeviceBean.from(result))
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                stop()
            }
        }

        override fun start(filters: MutableList<ScanFilter>?) {
            super.start(filters)
            val scanSettings = ScanSettings.Builder()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                scanSettings.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                scanSettings.setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                scanSettings.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            }

            BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner?.startScan(
                filters?.map {
                    Filter.Builder().setDeviceName(it.name).setDeviceAddress(it.mac).build()
                } ?: arrayListOf<Filter>(), scanSettings.build(), callBack
            )
        }

        override fun stop() {
            super.stop()
            BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner?.stopScan(callBack)
        }
    }

    @RequiresPermission(
        allOf = [android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_PRIVILEGED]
    )
    open fun start(filters: MutableList<ScanFilter>?) {
        listener?.onStart()
    }

    open fun stop() {
        listener?.onEnd(res)
    }

    open fun setScanListener(listener: BtScanListener? = null): BtScanner {
        this.listener = listener
        return this
    }
}