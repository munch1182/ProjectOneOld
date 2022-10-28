package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothDevice
import com.munch.lib.android.extend.SealedClassToStringByName

/**
 * Create by munch1182 on 2022/10/27 17:50.
 */

interface IBluetoothConnector {
    val state: BluetoothConnectState

    /**
     * 在[timeout]内是否连接成功
     *
     * @param timeout 连接超时时间, 该时间只对系统连接时间限制, 当系统连接后, 自定义操作不再限制超时时间, 或者说, 需要自行限定超时时间
     * @return 此次连接结果
     */
    suspend fun connect(timeout: Long = 30 * 1000L): BluetoothConnectResult

    /**
     * @param removeBond 是否需要移除系统的绑定
     *
     * @return 当[removeBond]为true时, 该结果返回是否解除成功, 否则固定返回true
     */
    suspend fun disconnect(removeBond: Boolean): Boolean

    fun addConnectListener(l: OnBluetoothConnectListener)
    fun removeConnectListener(l: OnBluetoothConnectListener)
}

/**
 * 提供原生的与蓝牙设备相关的操作对象
 */
interface IBluetoothDevManager {
    val dev: BluetoothDevice?
}

sealed class BluetoothConnectResult : SealedClassToStringByName() {
    object Success : BluetoothConnectResult() {
        override fun toString() = "BluetoothConnectSuccess"
    }

    class Fail(val reason: IBluetoothConnectFailReason) : BluetoothConnectResult() {
        override fun toString() = "BluetoothConnectFail($reason)"
    }

    val isSuccess: Boolean
        get() = this is Success
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
    val isDisconnect: Boolean
        get() = this is Disconnect
}

fun interface OnBluetoothConnectListener {
    fun onConnect(isSuccess: Boolean)
}

interface IBluetoothConnectFailReason {
    val code: Int
}

sealed class BluetoothConnectFailReason : SealedClassToStringByName(), IBluetoothConnectFailReason {
    object MacInvalid : BluetoothConnectFailReason()
    object NotFindDev : BluetoothConnectFailReason()
    object ConnectTimeout : BluetoothConnectFailReason()
    class SysErr(private val sysErrCode: Int) : BluetoothConnectFailReason() {
        override val code: Int
            get() = sysErrCode

        override fun toString() = "SysErr($sysErrCode)"
    }

    fun to() = BluetoothConnectResult.Fail(this)

    override val code: Int
        get() = 999 // 自定义的code都是999, 否则则应该是系统返回的code
}