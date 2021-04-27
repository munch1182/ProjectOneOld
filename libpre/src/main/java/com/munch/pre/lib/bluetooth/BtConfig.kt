package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService

/**
 * Create by munch1182 on 2021/4/8 10:56.
 */
open class BtConfig {

    companion object {
        const val MAX_MTU = 247
    }

    /**
     * 注意：如果此值过大，甚至不会回调onMtuChanged
     */
    open var mtu: Int = -1

    /**
     * 发现服务的回调
     *
     * 如果服务是必须的，则在此方法内寻找服务，寻找失败则应该返回false，否则返回true
     *
     * @return 当返回true时会回调连接成功，否则会连接回调失败
     */
    open fun onDiscoverService(
        device: BtDevice,
        gatt: BluetoothGatt,
        server: MutableList<BluetoothGattService>
    ): Boolean {
        return true
    }

    /**
     * 当mtu不为-1时，发现服务后会请求mtu并回调此方法
     *
     * 如果mtu值必须更改，则判断mtu是否更改成功，成功返回true，否则更改mtu再次进行[BluetoothGatt.requestMtu]或者返回false
     *
     * @return 当返回true时会回调连接成功，否则会连接回调失败
     */
    open fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int): Boolean {
        return mtu == this.mtu
    }

}