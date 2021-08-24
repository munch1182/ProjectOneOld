package com.munch.lib.bluetooth

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import com.munch.lib.base.Manageable

/**
 * Create by munch1182 on 2021/8/24 13:57.
 */
interface Scanner {
    fun startScan(timeOut: Long)

    fun stopScan()
}

interface OnScannerListener {

    fun onStart()
    fun onScan(device: BtDevice)
    fun onComplete(devices: MutableList<BtDevice>)
    fun onFail()
}

data class ScanFilter(val name:String?,val mac:String?)

class BleScanner : Manageable, Scanner {

    private val scanner: BluetoothLeScanner?
        get() = BluetoothHelper.instance.set.adapter?.bluetoothLeScanner

    override fun startScan(timeOut: Long) {
        /*scanner?.startScan()*/
    }

    override fun stopScan() {
    }

    override fun cancel() {
        stopScan()
    }

    override fun destroy() {
        stopScan()
    }
}