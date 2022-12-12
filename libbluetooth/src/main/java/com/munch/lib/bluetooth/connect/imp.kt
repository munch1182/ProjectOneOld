package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/10/27 18:02.
 */

internal object BluetoothConnectorConfig : IBluetoothHelperConnector {

    internal var builder = BluetoothConnector.Builder()

    /**
     * 对默认连接进行设置
     */
    override fun configConnect(build: BluetoothConnector.Builder.() -> Unit) {
        build.invoke(builder)
    }
}

internal abstract class BluetoothConnectImp(protected val dev: BluetoothDevice) :
    IBluetoothConnector,
    ARSHelper<OnBluetoothConnectStateListener>(),
    IBluetoothHelperEnv by BluetoothHelperEnv {

    private val lock = Mutex()
    protected open var _connectState: BluetoothConnectState = getInitConnectState()
        get() = runBlocking { lock.withLock { field } }
        set(value) = runBlocking {
            lock.withLock {
                val last = field
                if (field != value) {
                    field = value
                    update { it.onConnectState(last, value) }
                }
            }
        }

    private fun getInitConnectState(): BluetoothConnectState {
        if (BluetoothHelper.connectDevs?.contains(dev) == true) {
            return BluetoothConnectState.Connected
        }
        return BluetoothConnectState.Disconnected
    }

    override val connectState: BluetoothConnectState
        get() = _connectState

    override fun addConnectListener(l: OnBluetoothConnectStateListener) {
        add(l)
    }

    override fun removeConnectListener(l: OnBluetoothConnectStateListener) {
        remove(l)
    }

}

internal class BluetoothLeConnectImp(dev: BluetoothDevice) : BluetoothConnectImp(dev),
    BluetoothGattHelper.OnConnectStateChangeListener {

    private val gattHelper by lazy {
        BluetoothGattHelper(dev).setConnectStateListener(this)
    }
    private var _gatt: BluetoothGatt? = null
    private var _onConnect: BluetoothGattHelper.OnConnectStateChangeListener? = null

    override suspend fun connect(
        timeout: Long,
        config: BluetoothConnector.Builder?
    ): BluetoothConnectResult {
        val b = config ?: BluetoothConnectorConfig.builder
        val result = com.munch.lib.android.extend.suspendCancellableCoroutine(timeout) {
            _onConnect = BluetoothGattHelper.OnConnectStateChangeListener { status, newState ->
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        _onConnect = null
                        log.log("$dev connect gatt: Success.")
                        it.resume(BluetoothConnectResult.Success)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        _onConnect = null
                        log.log("connect gatt: fail.")
                        val reason = BluetoothConnectFailReason.SysErr(status).toReason()
                        it.resume(reason)
                    }
                    else -> {
                        //wait
                    }
                }
            }
            log.log("$dev start CONNECT gatt.")
            _gatt = dev.connectGatt(
                null, false,
                gattHelper.callback, b.transport, b.phy
            )
        } ?: timeoutDisconnect()
        val judge = b.judge
        if (result == BluetoothConnectResult.Success && judge != null) {
            log.log("$dev start custom JUDGE.")
            val judgeResult = judge.onJudge(gattHelper)
            log.log("$dev onJudge: $judgeResult.")
            return judgeResult
        }

        log.log("$dev connect result: $result.")
        return result
    }

    private suspend fun timeoutDisconnect(): BluetoothConnectResult {
        disconnect(false)
        return BluetoothConnectFailReason.ConnectTimeout.toReason()
    }

    override suspend fun disconnect(removeBond: Boolean): Boolean {
        if (_gatt != null) {
            log.log("$dev start DISCONNECT gatt.")
            _gatt?.disconnect()
            var index = 5
            while (index > 0) {
                delay(200L)
                if (BluetoothHelper.isConnect(dev) == false) {
                    break
                }
                index--
            }
        }
        return true
    }

    override fun onConnectStateChange(status: Int, newState: Int) {
        _onConnect?.onConnectStateChange(status, newState)
        _connectState = BluetoothConnectState.from(newState)
    }
}

internal class BluetoothClassicConnectImp(dev: BluetoothDevice) : BluetoothConnectImp(dev) {
    override suspend fun connect(
        timeout: Long,
        config: BluetoothConnector.Builder?
    ): BluetoothConnectResult {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(removeBond: Boolean): Boolean {
        TODO("Not yet implemented")
    }


}
