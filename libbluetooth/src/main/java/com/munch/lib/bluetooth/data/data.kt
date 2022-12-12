package com.munch.lib.bluetooth.data

/**
 * Create by munch1182 on 2022/12/12 14:07.
 */


interface IBluetoothDataHandler {

    /**
     * 发送一包完整的数据
     *
     * 超出mtu大小的数据会被自动分包
     */
    suspend fun send(pack: ByteArray): Boolean

    /**
     * 设置数据接收的回调
     */
    fun setDataReceiver(receiver: BluetoothDataReceiver)
}

interface BluetoothDataReceiver {
    /**
     * 收到一包原始数据的回调
     *
     * 这一次的数据可能并非一包完整的数据
     */
    suspend fun onDataReceived(data: ByteArray)
}