package com.munch.project.one.bluetooth

import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.extend.SealedClassToStringByName
import com.munch.lib.recyclerview.ItemNode


internal sealed class BleIntent : SealedClassToStringByName() {

    object StartOrStopScan : BleIntent()
    object Destroy : BleIntent()
}

internal sealed class BleUIState : SealedClassToStringByName() {

    class Data(val data: List<Dev>) : BleUIState()
    object StartScan : BleUIState()
    object StopScan : BleUIState()

    object None : BleUIState()
}

internal sealed class Dev {

    companion object {
        const val TYPE_BLE = 0
        const val TYPE_RECORD = 1
    }

    class Ble(val ble: BluetoothDev) : Dev(), ItemNode {
        override fun getItemType(pos: Int) = TYPE_BLE
        override var isExpand: Boolean = false
        override val children: List<ItemNode> = mutableListOf()
    }

    class Record(val record: String) : Dev(), ItemNode {
        override fun getItemType(pos: Int) = TYPE_RECORD
    }
}