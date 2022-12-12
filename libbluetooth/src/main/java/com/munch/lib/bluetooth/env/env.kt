package com.munch.lib.bluetooth.env

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * Create by munch1182 on 2022/10/26 16:28.
 */


/**
 * 提供蓝牙管理对象
 */
interface IBluetoothManager {
    /**
     * 提供一个系统的BluetoothManager对象
     */
    val bm: BluetoothManager?

    /**
     * 提供一个系统的BluetoothAdapter对象
     */
    val adapter: BluetoothAdapter?
}

/**
 * 提供蓝牙相关状态
 */
interface IBluetoothState {
    /**
     * 是否支持BLE
     */
    val isSupportBle: Boolean

    /**
     * 蓝牙是否开启
     */
    val isEnable: Boolean

    /**
     * 系统蓝牙已配对设备
     */
    val pairedDevs: Set<BluetoothDevice>?

    /**
     * 系统蓝牙已连接设备
     */
    val connectDevs: List<BluetoothDevice>?
        get() = pairedDevs?.filter { isConnect(it) ?: false }

    /**
     * 该蓝牙是否已连接
     */
    fun isConnect(device: BluetoothDevice): Boolean?

    /**
     * 回调蓝牙通知
     */
    fun addStateChangeListener(l: OnBluetoothStateNotifyListener?)
    fun removeStateChangeListener(l: OnBluetoothStateNotifyListener?)
}

/**
 * 蓝牙通知的回调
 */
fun interface OnBluetoothStateNotifyListener {
    /**
     * 当收到系统蓝牙的更改时进行通知
     */
    fun onStateNotify(state: BluetoothNotify, mac: String?)
}

/**
 * 系统蓝牙的状态变更通知
 */
sealed class BluetoothNotify : SealedClassToStringByName() {
    /**
     * 蓝牙打开的通知
     */
    object StateOn : BluetoothNotify()

    /**
     * 蓝牙关闭的通知
     */
    object StateOff : BluetoothNotify()

    /**
     * 蓝牙绑定成功的通知
     */
    class Bonded(val mac: String?) : BluetoothNotify()

    /**
     * 蓝牙绑定中的通知
     */
    class Bonding(val mac: String?) : BluetoothNotify()

    /**
     * 蓝牙绑定失败的通知
     */
    class BondFail(val mac: String?) : BluetoothNotify()
}
