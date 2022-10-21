package com.munch.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
open class BluetoothDev(override val mac: String, var name: String?, dev: BluetoothDevice? = null) :
    IBluetoothDev {

    private var dev: BluetoothDevice? = dev

}

class BluetoothScanDev(scan: ScanResult) :
    BluetoothDev(scan.device.address, scan.device.name, scan.device)