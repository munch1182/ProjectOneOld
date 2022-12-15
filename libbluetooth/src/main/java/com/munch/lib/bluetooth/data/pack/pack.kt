package com.munch.lib.bluetooth.data.pack

import com.munch.lib.android.extend.to
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.dev.BluetoothDev
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/12/15 16:15.
 */
interface IBluetoothDataPack {

    /**
     * 此数据包的唯一标识
     */
    val id: Int
}

class BluetoothDataPackWrapper(val pack: IBluetoothDataPack) {
    /**
     * 等待数据回复的超时时间
     */
    var timeout: Long = 5000L

    /**
     * 期待响应的数据包
     *
     * 如果为0, 则不需要响应
     */
    var wantID: Int = 0

    /**
     * 判断该包是否需要的回复数据
     */
    fun judgeResponse(pack: IBluetoothDataPack): Boolean {
        return pack.id == wantID
    }

    /**
     * 判断此请求是否需要回复
     */
    fun needResponse() = wantID != 0
}

interface IBluetoothBytePackConvert<T : IBluetoothDataPack> {
    /**
     * 将byteArray转换成结构数据
     *
     * 如果该byteArray无法解析或者是一个结构的一部分, 可以返回null
     * [BluetoothDev.setReceiver]只会回调此方法不为null的部分
     */
    fun from(byteArray: ByteArray): T?

    /**
     * 将结构数据转为可发送的字节数据
     */
    fun to(pack: T): ByteArray
}

internal object BluetoothPackHelper {
    internal var convert: IBluetoothBytePackConvert<IBluetoothDataPack>? = null
}

fun <T : IBluetoothDataPack> BluetoothHelper.configConvert(convert: IBluetoothBytePackConvert<T>) {
    BluetoothPackHelper.convert = convert.to()
}

fun interface BluetoothDataReceiver<T : IBluetoothDataPack> {
    /**
     * 收到一包数据的回调
     */
    suspend fun onReceived(data: T)
}

/**
 * 发送数据, 并等待响应
 */
suspend fun <T : IBluetoothDataPack> BluetoothDev.send(pack: BluetoothDataPackWrapper): T? =
    suspendCancellableCoroutine { con ->
        BluetoothHelper.launch {
            val receiver = object : BluetoothDataReceiver<T> {
                override suspend fun onReceived(data: T) {
                    if (data.id == pack.wantID) {
                        removeReceiver(this)
                        if (con.isActive) con.resume(data)
                    }
                }
            }
            addReceiver(receiver)
            send(
                BluetoothPackHelper.convert?.to(pack.pack)
                    ?: throw IllegalArgumentException("must set Convert by BluetoothHelper.configConvert")
            )
            withTimeoutOrNull(pack.timeout) {
                removeReceiver(receiver)
                if (con.isActive) con.resume(null)
            }
        }
    }

fun <T : IBluetoothDataPack> BluetoothDev.setReceiver(receiver: BluetoothDataReceiver<T>?) {
    if (receiver == null) {
        setReceiver(null as com.munch.lib.bluetooth.data.BluetoothDataReceiver?)
    } else {
        setReceiver {
            val dataPack = BluetoothPackHelper.convert?.from(it)
            dataPack?.let { p -> receiver.onReceived(p.to()) }
        }
    }
}

fun <T : IBluetoothDataPack> BluetoothDev.addReceiver(receiver: BluetoothDataReceiver<T>) {
    setReceiver {
        val dataPack = BluetoothPackHelper.convert?.from(it)
        dataPack?.let { p -> receiver.onReceived(p.to()) }
    }
}

fun <T : IBluetoothDataPack> BluetoothDev.removeReceiver(receiver: BluetoothDataReceiver<T>) {
    // todo
}