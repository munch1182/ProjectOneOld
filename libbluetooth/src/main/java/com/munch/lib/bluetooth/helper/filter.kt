package com.munch.lib.bluetooth.helper

import android.util.SparseArray
import com.munch.lib.bluetooth.dev.BluetoothDev
import com.munch.lib.bluetooth.dev.BluetoothScannedDev
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2022/10/26 17:34.
 */

/**
 * 过滤掉没有名字的设备
 */
class BluetoothDevNoNameFilter : OnBluetoothDevFilter {
    override fun isNeedBeFilter(dev: BluetoothDev): Boolean {
        if (dev !is BluetoothScannedDev) return true
        return dev.name.isNullOrBlank()
    }
}

/**
 * 扫描到的设备只返回第一次, 后续再扫描到不再返回
 */
class BluetoothDevFirstFilter : OnBluetoothDevLifecycleFilter {
    private val map = SparseArray<String>()
    override fun isNeedBeFilter(dev: BluetoothDev): Boolean {
        if (map.indexOfKey(dev.mac.hashCode()) > -1) {
            return true
        }
        map.put(dev.mac.hashCode(), dev.mac)
        return false
    }

    override fun onStop() {
        super.onStop()
        map.clear()
    }
}

/**
 * 为了寻找特定[mac]地址的设备, 过滤掉其它所有的设备
 */
class BluetoothDevFindFilter(private val mac: String) : OnBluetoothDevFilter {
    override fun isNeedBeFilter(dev: BluetoothDev): Boolean {
        return dev.mac != mac
    }
}

/**
 * 过滤设备名称不包含[name]的所有设备
 */
class BluetoothDevNameFindFilter(private val name: String) : OnBluetoothDevFilter {
    override fun isNeedBeFilter(dev: BluetoothDev): Boolean {
        if (dev is BluetoothScannedDev) {
            return !(dev.name?.contains(name, true) ?: false)
        }
        return true
    }
}

/**
 * 过滤设备地址不包含[mac]的所有设备
 */
class BluetoothDevMacFindFilter(private val mac: String) : OnBluetoothDevFilter {
    override fun isNeedBeFilter(dev: BluetoothDev): Boolean {
        return !dev.mac.contains(mac, true)
    }
}

/**
 * 过滤信号值小于[rssi]的所有设备
 */
class BluetoothDevRssiFindFilter(private val rssi: Int) : OnBluetoothDevFilter {
    override fun isNeedBeFilter(dev: BluetoothDev): Boolean {
        if (dev !is BluetoothScannedDev) return true
        val devRssi = dev.rssi ?: return false
        return devRssi.absoluteValue > rssi.absoluteValue
    }
}

