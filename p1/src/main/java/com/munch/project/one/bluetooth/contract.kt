package com.munch.project.one.bluetooth

import com.munch.lib.bluetooth.BluetoothDev
import com.munch.lib.extend.SealedClassToStringByName
import com.munch.lib.recyclerview.ItemNode


internal sealed class BleIntent : SealedClassToStringByName() {

    object StartOrStopScan : BleIntent()
    object StopScan : BleIntent()
    object Destroy : BleIntent()
}

internal sealed class BleUIState : SealedClassToStringByName() {

    class Data(val data: List<Dev>) : BleUIState()
    object StartScan : BleUIState()
    object StopScan : BleUIState()

    object None : BleUIState()
}

internal sealed class Dev : ItemNode {

    companion object {
        const val TYPE_BLE = 0
        const val TYPE_RECORD = 1
    }

    override var isExpand: Boolean = false
    open fun onContentSame(dev: Dev) = false
    open fun onItemSame(dev: Dev) = false

    class Ble(val ble: BluetoothDev) : Dev() {
        override fun getItemType(pos: Int) = TYPE_BLE
        override val children: List<ItemNode> =
            ble.scanRecord?.let { listOf(Record(it)) } ?: listOf()

        override fun onContentSame(dev: Dev): Boolean {
            if (dev !is Ble) return false
            return dev.ble.rssi == ble.rssi
        }

        override fun onItemSame(dev: Dev): Boolean {
            if (dev !is Ble) return false
            return dev.ble.mac == ble.mac
        }
    }

    class Record(val record: ByteArray?) : Dev() {
        override fun getItemType(pos: Int) = TYPE_RECORD

        override fun onItemSame(dev: Dev): Boolean {
            if (dev !is Record) return false
            return this == dev
        }

        override fun onContentSame(dev: Dev): Boolean {
            return true
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Record
            if (record != null) {
                if (other.record == null) return false
                if (!record.contentEquals(other.record)) return false
            } else if (other.record != null) return false
            return true
        }

        override fun hashCode(): Int {
            return record?.contentHashCode() ?: 0
        }


    }
}