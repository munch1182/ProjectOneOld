package com.munch.lib.bluetooth

import android.bluetooth.le.ScanResult

/**
 * Create by munch1182 on 2022/9/29 15:47.
 */
open class BluetoothDev(override val mac: String) : IBluetoothDev

class BluetoothScanDev(val scan: ScanResult) : BluetoothDev(scan.device.address)