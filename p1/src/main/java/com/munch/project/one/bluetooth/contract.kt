package com.munch.project.one.bluetooth

import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.extend.times
import com.munch.lib.recyclerview.ItemNode
import kotlin.random.Random


internal sealed class BleIntent {

    object Refresh : BleIntent()
    object Request : BleIntent()

}

internal sealed class BleUIState {

    class Data(val data: List<Dev>) : BleUIState()
}

internal sealed class Dev {

    companion object {
        const val TYPE_BLE = 0
        const val TYPE_RECORD = 1
    }

    class Ble(val ble: BluetoothDev) : Dev(), ItemNode {
        override fun getItemType(pos: Int) = TYPE_BLE
        override var isExpand: Boolean = false
        override val children: List<ItemNode> =
            MutableList(Random.nextInt(4) + 2) { Record(it.toString() * 5) }
    }

    class Record(val record: String) : Dev(), ItemNode {
        override fun getItemType(pos: Int) = TYPE_RECORD
    }
}