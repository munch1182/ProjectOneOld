package com.munch.lib.bluetooth.data

/**
 * Create by munch1182 on 2022/12/12 14:07.
 */

interface IBluetoothDataHandler {

    /**
     * 发送一包完整的数据, 数据实际发出的顺序是按照调用的顺序的, 所以最好在单线程内调用, 避免顺序混乱
     *
     * 超出mtu大小的数据会被自动分包
     *
     * @return 发送是否成功, 如果当前未连接必定返回失败
     */
    suspend fun send(pack: ByteArray): Boolean

    /**
     * 设置一个数据接收回调
     *
     * @param receiver 与[com.munch.lib.bluetooth.data.IBluetoothDataDispatcher.addReceiver]不同, 此回调是设备唯一的
     */
    fun setReceiver(receiver: BluetoothDataReceiver?)

    /**
     * 取消当前的数据发送
     *
     * 取消当前发送队列中的数据发送, 此方法会取消所有未发送的数据
     */
    fun cancelSend()
}

interface IBluetoothDataHandlerManager {
    fun active()
    fun inactive()
}

interface IBluetoothDataManger : IBluetoothDataHandler,
    IBluetoothDataHandlerManager,
    IBluetoothDataDispatcher

fun interface BluetoothDataReceiver {
    /**
     * 收到一包原始数据的回调
     */
    suspend fun onReceived(data: ByteArray)
}


/**
 * 用于转换
 */
interface IBluetoothDataPack {

    fun toBytes(): ByteArray
}

interface IBluetoothDataDispatcher {
    fun addReceiver(receiver: BluetoothDataReceiver)
    fun removeReceiver(receiver: BluetoothDataReceiver)
}