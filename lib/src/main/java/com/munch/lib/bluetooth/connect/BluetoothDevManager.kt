package com.munch.lib.bluetooth.connect

import android.util.ArrayMap
import com.munch.lib.bluetooth.BluetoothDev

/**
 * 注意：此方法是依赖mac地址索引的
 *
 * Create by munch1182 on 2021/12/8 14:13.
 */
class BluetoothDevManager {

    private val connectedDevPool: ArrayMap<String, BluetoothDev> = ArrayMap(6)

    /**
     * 当连接成功时添加
     */
    fun manage(dev: BluetoothDev) {
        connectedDevPool[dev.mac] = dev
    }

    /**
     * 当断开连接时移除
     */
    fun discard(dev: BluetoothDev) {
        if (connectedDevPool.contains(dev.mac)) {
            connectedDevPool.remove(dev.mac)
        }
    }

    fun get(mac: String): BluetoothDev? {
        return connectedDevPool[mac]
    }
}