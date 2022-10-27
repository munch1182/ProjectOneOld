package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothGatt
import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * Create by munch1182 on 2022/10/27 17:50.
 */

interface IBluetoothConnector {
    val state: BluetoothConnectState

    fun connect(timeout: Long = 30 * 1000L)
    fun disconnect()

    fun addConnectListener(l: OnBluetoothConnectListener)
    fun removeConnectListener(l: OnBluetoothConnectListener)
}

sealed class BluetoothConnectState : SealedClassToStringByName() {
    /**
     * 未连接
     */
    object Disconnect : BluetoothConnectState()

    /**
     * 连接中
     *
     * 注意: 此连接中可能为系统未回调已连接时的阶段, 也可能时系统已连接但自定义操作未完成的阶段, 需要看具体实现的类
     */
    object Connecting : BluetoothConnectState()

    /**
     * 已连接
     */
    object Connected : BluetoothConnectState()

    val isConnected: Boolean
        get() = this is Connected
    val isConnecting: Boolean
        get() = this is Connecting
}

fun interface OnBluetoothConnectListener {
    fun onConnect(isSuccess: Boolean, connectHelper: BluetoothConnectHelper?)
}

/**
 * 一个连接帮助类, 用以处理连接后的相关的操作
 * 该类只能在连接成功后生成, 且在断开连接后应该销毁对象
 */
class BluetoothConnectHelper(
    private val gatt: BluetoothGatt // gatt对象, 只有不为null才能构建BluetoothConnectHelper对象
)