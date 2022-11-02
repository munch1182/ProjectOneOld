package com.munch.lib.bluetooth.helper

import com.munch.lib.bluetooth.dev.BluetoothDev

/**
 * Create by munch1182 on 2022/11/2 10:13.
 */

internal interface IBluetoothHelperDev {

    fun getDev(mac: String): BluetoothDev
}