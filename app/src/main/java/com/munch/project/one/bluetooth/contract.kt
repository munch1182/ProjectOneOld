package com.munch.project.one.bluetooth

import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.bluetooth.BluetoothDev

/**
 * Create by munch1182 on 2022/9/24 14:32.
 */
sealed class BluetoothIntent : SealedClassToStringByName() {

    object StartScan : BluetoothIntent()
    object StopScan : BluetoothIntent()

    object ToggleScan : BluetoothIntent()
}

sealed class BluetoothState : SealedClassToStringByName() {
    class ScannedDevs(val data: List<BluetoothDev>) : BluetoothState()
    class IsScan(val isScan: Boolean) : BluetoothState()
}