package com.munch.project.one.bluetooth

import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.bluetooth.dev.BluetoothDev
import com.munch.lib.bluetooth.helper.*

/**
 * Create by munch1182 on 2022/9/24 14:32.
 */
sealed class BluetoothIntent : SealedClassToStringByName() {

    object StartScan : BluetoothIntent()
    object StopScan : BluetoothIntent()

    object ToggleScan : BluetoothIntent()
    class UpdateFilter(val f: BluetoothFilter) : BluetoothIntent()

    class Connect(val dev: BluetoothDev) : BluetoothIntent()
}

sealed class BluetoothState : SealedClassToStringByName() {
    class ScannedDevs(val data: List<BluetoothDev>) : BluetoothState()
    class IsScan(val isScan: Boolean) : BluetoothState()
    class FilterUpdate(val f: BluetoothFilter) : BluetoothState()
}

data class BluetoothFilter(
    var name: String? = null,
    var mac: String? = null,
    var rssi: Int = -100,
    var noName: Boolean = true,
    var isBle: Boolean = true,
) {

    companion object {
        fun from(f: BluetoothFilterView.Filter): BluetoothFilter {
            return BluetoothFilter(f.name, f.mac, f.rssi ?: -100, f.noName ?: true, f.isBle ?: true)
        }
    }

    fun toViewFilter(): BluetoothFilterView.Filter {
        return BluetoothFilterView.Filter(name, mac, rssi, noName, isBle)
    }

    fun to(): OnBluetoothDevFilter? {
        val list = mutableListOf<OnBluetoothDevFilter>()
        if (!name.isNullOrBlank()) {
            list.add(BluetoothDevNameFindFilter(name!!))
        }
        if (!mac.isNullOrBlank()) {
            list.add(BluetoothDevMacFindFilter(mac!!))
        }
        if (rssi != -100) {
            list.add(BluetoothDevRssiFindFilter(rssi))
        }
        if (noName) {
            list.add(BluetoothDevNoNameFilter())
        }
        if (list.isEmpty()) {
            return null
        }
        return BluetoothDevFilterContainer(*list.toTypedArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BluetoothFilter
        if (name != other.name) return false
        if (mac != other.mac) return false
        if (rssi != other.rssi) return false
        if (noName != other.noName) return false
        if (isBle != other.isBle) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (mac?.hashCode() ?: 0)
        result = 31 * result + rssi
        result = 31 * result + noName.hashCode()
        result = 31 * result + isBle.hashCode()
        return result
    }


}