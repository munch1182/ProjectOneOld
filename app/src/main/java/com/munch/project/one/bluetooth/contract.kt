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
    class Filter(val f: BluetoothFilter) : BluetoothIntent()
}

sealed class BluetoothState : SealedClassToStringByName() {
    class ScannedDevs(val data: List<BluetoothDev>) : BluetoothState()
    class IsScan(val isScan: Boolean) : BluetoothState()
}

data class BluetoothFilter(
    var name: String? = null,
    var mac: String? = null,
    var rssi: Int = -100,
    var noName: Boolean = true
) {

    companion object {
        fun from(f: BluetoothFilterView.Filter): BluetoothFilter {
            return BluetoothFilter(f.name, f.mac, f.rssi, f.noName)
        }
    }
}