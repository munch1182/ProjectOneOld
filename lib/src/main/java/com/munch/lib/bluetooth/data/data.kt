package com.munch.lib.bluetooth.data

/**
 * Create by munch1182 on 2021/12/9 14:49.
 */

interface IData {

    fun send(byteArray: ByteArray)

    fun onReceived(received: OnByteArrayReceived)
}

/**
 * 用于转为实体类与蓝牙字节数组
 */
interface IBluetoothDataConvert<T> {

    fun send(data: T?): ByteArray

    fun onReceived(byteArray: ByteArray): T?
}

typealias OnByteArrayReceived = (byteArray: ByteArray) -> Unit