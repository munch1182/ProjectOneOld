package com.munch.lib.bluetooth

import android.bluetooth.le.ScanResult

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
class BluetoothDev(override val mac: String) : IBluetoothDev

class BluetoothScanDev(val result: ScanResult) : IBluetoothDev {
    override val mac: String
        get() = result.device.address
}