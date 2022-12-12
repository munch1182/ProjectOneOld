package com.munch.lib.bluetooth.data

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Create by munch1182 on 2022/12/12 14:07.
 */


interface IBluetoothDataHandler {

    val receive: ReceiveChannel<ByteArray>

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
     *
     * @return 将原始数据包进行判断、拼装或者解密等操作, 返回逻辑上的一包完整数据, 这些数据会返回到[IBluetoothDataHandler.receive]中
     * 如果此包不是一个完整的包, 则可以返回null, [IBluetoothDataHandler.receive]只会发送此方法返回的不为null的部分
     *
     * 如果不需要这样处理, 也可以从此处自行回调数据
     * 默认什么都不处理原样返回
     */
    suspend fun onDataReceived(data: ByteArray): ByteArray? = data
}