package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothGatt
import com.munch.lib.android.AppHelper
import com.munch.lib.android.helper.ARSHelper
import com.munch.lib.bluetooth.dev.BluetoothDev
import com.munch.lib.bluetooth.env.BluetoothEnv
import com.munch.lib.bluetooth.env.IBluetoothManager
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import com.munch.lib.bluetooth.connect.BluetoothConnectFailReason as REASON
import com.munch.lib.bluetooth.connect.BluetoothGattHelper.OnConnectStateChangeListener as STATELISTENER

/**
 * Create by munch1182 on 2022/10/27 18:02.
 */

internal object BluetoothHelperConnectorConfig : IBluetoothHelperConnector {

    internal var builder = BluetoothHelperConnector.Builder()

    /**
     * 对默认连接进行设置
     */
    override fun configConnect(build: BluetoothHelperConnector.Builder.() -> Unit) {
        build.invoke(builder)
    }
}

/**
 * 处理连接动作以及状态维护
 */
internal class BluetoothConnector(private val mac: String) :
    IBluetoothManager by BluetoothEnv,
    IBluetoothHelperEnv by BluetoothHelperEnv,
    ARSHelper<OnBluetoothConnectStateListener>(),
    STATELISTENER {

    private val lock = Mutex()
    private var _gatt: BluetoothGatt? = null
    internal var gattHelper: BluetoothGattHelper? = null
    private var stateListener: STATELISTENER? = null
    private var _connectState: BluetoothConnectState = BluetoothConnectState.Disconnected
        set(value) = runBlocking {
            val last = field
            lock.withLock { field = value }
            log("Connect State: $last -> $value")
            update { it.onConnectState(last, value) }
        }

    val connectState: BluetoothConnectState
        get() = _connectState

    suspend fun connect(dev: BluetoothDev, timeout: Long): BluetoothConnectResult {
        if (_connectState.isConnected) {
            return REASON.ConnectedButConnect.to()
        }
        var sysDev = dev.dev

        updateState(BluetoothConnectState.Connecting)

        if (sysDev == null) {
            log("start FIND dev")
            val find = dev.find(timeout) // 会更改dev.dev
            if (find == null) {

                updateState(BluetoothConnectState.Disconnected)

                log("FIND nothing")
                return REASON.NotFindDev.to()
            } else {
                log("FIND device")
            }
        }
        sysDev = dev.dev
        if (sysDev == null) {

            updateState(BluetoothConnectState.Disconnected)

            return REASON.NotFindDev.to()
        }
        val helper = BluetoothGattHelper(sysDev)
        helper.setConnectStateListener(this)
        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine { continuation ->
                stateListener = STATELISTENER { status, newState ->
                    var result = when (newState) {
                        BluetoothGatt.STATE_CONNECTED -> BluetoothConnectResult.Success
                        BluetoothGatt.STATE_DISCONNECTING, BluetoothGatt.STATE_DISCONNECTED ->
                            REASON.SysErr(status).to()
                        else -> null
                    } ?: return@STATELISTENER

                    stateListener = null // 不再接收结果

                    if (result.isSuccess) {
                        val judge = BluetoothHelperConnectorConfig.builder.judge

                        log("connect result from system: $result")

                        gattHelper = helper // 连接成功才赋值
                        log("BluetoothGattHelper is set")

                        if (judge != null) {
                            result = runBlocking {
                                val judgeRust = judge.onJudge(helper)

                                log("connect result from judge: $result")
                                judgeRust
                            }
                        }

                        if (result.isSuccess) {
                            updateState(BluetoothConnectState.Connected)
                        }

                    } else {
                        runBlocking { disconnect(dev, true) } // 连接失败则断开, 会更改连接状态
                    }

                    log("connect result: $result")

                    continuation.resume(result)
                }
                log("start CONNECT with timeout $timeout ms")
                _gatt = sysDev.connectGatt(AppHelper, false, helper.callback)
            }
        } ?: let {
            log("TIMEOUT to connect")
            stateListener = null
            disconnect(dev, true)
            REASON.ConnectTimeout.to()
        }
    }

    suspend fun disconnect(dev: BluetoothDev, removeBond: Boolean): Boolean {
        if (_gatt != null) {
            log("DISCONNECT")
            updateState(BluetoothConnectState.Disconnecting)
        }
        _gatt?.disconnect()
        _gatt?.close() // todo 在断开之后再close
        _gatt = null
        updateState(BluetoothConnectState.Disconnected)
        return if (removeBond) dev.removeBond() else true
    }

    private fun updateState(state: BluetoothConnectState) {
        _connectState = state
    }

    override fun log(content: String) {
        if (BluetoothHelperConfig.builder.enableLog) {
            log.log("[$mac]: $content.")
        }
    }

    override fun onConnectStateChange(status: Int, newState: Int) {
        launch { // 此时的线程为系统回调线程, 改为蓝牙线程来处理
            stateListener?.onConnectStateChange(status, newState)
            val state = BluetoothConnectState.from(newState)
            if (state != _connectState) {
                _connectState = state
            }
        }

    }
}
